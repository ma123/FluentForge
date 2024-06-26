package com.identic.fluentforge.dataReader.remote.repo

import com.google.gson.Gson
import com.identic.fluentforge.dataReader.remote.repo.models.BookSet
import com.identic.fluentforge.dataReader.remote.repo.models.ExtraInfo
import com.identic.fluentforge.dataReader.remote.utils.book.BookLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class BookRepository {

    private val baseApiUrl = "https://myne.pooloftears.xyz/books"
    private val googleBooksUrl = "https://www.googleapis.com/books/v1/volumes"
    private val googleApiKey = "AIzaSyBCaXx-U0sbEpGVPWylSggC4RaR4gCGkVE"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(100, TimeUnit.SECONDS)
        .build()

    private val gsonClient = Gson()

    suspend fun getAllBooks(
        page: Long,
        bookLanguage: BookLanguage = BookLanguage.AllBooks
    ): Result<BookSet> {
        var url = "${baseApiUrl}?page=$page"
        if (bookLanguage != BookLanguage.AllBooks) {
            url += "&languages=${bookLanguage.isoCode}"
        }
        val request = Request.Builder().get().url(url).build()
        return makeApiRequest(request)
    }

    suspend fun searchBooks(query: String): Result<BookSet> {
        val encodedString = withContext(Dispatchers.IO) {
            URLEncoder.encode(query, "UTF-8")
        }
        val request = Request.Builder().get().url("${baseApiUrl}?search=$encodedString").build()
        return makeApiRequest(request)
    }

    suspend fun getBookById(bookId: String): Result<BookSet> {
        val request = Request.Builder().get().url("${baseApiUrl}?ids=$bookId").build()
        return makeApiRequest(request)
    }

    private suspend fun makeApiRequest(request: Request): Result<BookSet> =
        suspendCoroutine { continuation ->
            okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resume(Result.failure(exception = e))
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        continuation.resume(
                            Result.success(
                                gsonClient.fromJson(response.body!!.string(), BookSet::class.java)
                            )
                        )
                    }
                }
            })
        }

    suspend fun getExtraInfo(bookName: String): ExtraInfo? = suspendCoroutine { continuation ->
        val encodedName = URLEncoder.encode(bookName, "UTF-8")
        val url = "${googleBooksUrl}?q=$encodedName&startIndex=0&maxResults=1&key=$googleApiKey"
        val request = Request.Builder().get().url(url).build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                continuation.resumeWithException(e)
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    continuation.resume(parseExtraInfoJson(response.body!!.string()))
                }
            }
        })
    }

    fun parseExtraInfoJson(jsonString: String): ExtraInfo? {
        val jsonObj = JSONObject(jsonString)
        return try {
            val totalItems = jsonObj.getInt("totalItems")
            if (totalItems != 0) {
                val items = jsonObj.getJSONArray("items")
                val item = items.getJSONObject(0)
                val volumeInfo = item.getJSONObject("volumeInfo")
                val imageLinks = volumeInfo.getJSONObject("imageLinks")
                // Build Extra info.
                val coverImage = imageLinks.getString("thumbnail")
                val pageCount = try {
                    volumeInfo.getInt("pageCount")
                } catch (exc: JSONException) {
                    0
                }
                val description = try {
                    volumeInfo.getString("description")
                } catch (exc: JSONException) {
                    ""
                }
                ExtraInfo(coverImage, pageCount, description)
            } else {
                null
            }
        } catch (exc: JSONException) {
            exc.printStackTrace()
            null
        }
    }
}