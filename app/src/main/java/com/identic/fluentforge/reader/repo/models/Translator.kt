package com.identic.fluentforge.reader.repo.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Translator(
    @SerializedName("name")
    val name: String = "N/A",
    @SerializedName("birth_year")
    val birthYear: Int,
    @SerializedName("death_year")
    val deathYear: Int
)