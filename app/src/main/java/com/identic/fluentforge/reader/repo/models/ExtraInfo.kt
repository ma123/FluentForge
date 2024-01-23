package com.identic.fluentforge.reader.repo.models

import androidx.annotation.Keep

/** Extra info from google books API */
@Keep
data class ExtraInfo(
    val coverImage: String = "",
    val pageCount: Int = 0,
    val description: String = ""
)
