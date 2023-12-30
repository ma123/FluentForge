package com.identic.fluentforge.data

import com.identic.fluentforge.data.remote.dto.InternetRadioDto
import retrofit2.http.GET
import retrofit2.http.Query

interface InternetRadioApi {
    @GET("search?")
    suspend fun getInternetRadiosData(
        @Query("page") page: Int = 1,
        @Query("order") order: String = "votes",
        @Query("reverse") reverse: Boolean = true,
        @Query("hidebroken") hidebroken: Boolean = true,
        @Query("language") language: String = "english",
        @Query("languageExact") languageExact: Boolean = true,
        @Query("countryExact") countryExact: Boolean = true,
        @Query("countrycode") countrycode: String = "GB",
        @Query("is_https") isHttps: Boolean = true,
        @Query("limit") limit: Int = 30
        //@Query("offset") offset: Int = 20
        //offset if we want paging something like pokemon app
    ): List<InternetRadioDto>
}