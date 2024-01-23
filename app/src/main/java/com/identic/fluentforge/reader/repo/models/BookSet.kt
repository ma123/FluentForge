package com.identic.fluentforge.reader.repo.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.identic.fluentforge.reader.repo.models.Book

@Keep
data class BookSet(
    @SerializedName("count")
    val count: Int,
    @SerializedName("next")
    val next: String?,
    @SerializedName("previous")
    val previous: String?,
    @SerializedName("results")
    val books: List<Book>
)