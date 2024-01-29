package com.identic.fluentforge.dataReader.remote.repo.models


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Author(
    @SerializedName("name")
    val name: String = "N/A",
    @SerializedName("birth_year")
    val birthYear: Int,
    @SerializedName("death_year")
    val deathYear: Int
)