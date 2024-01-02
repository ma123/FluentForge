package com.identic.fluentforge.dataRadio.remote.dto


import com.google.gson.annotations.SerializedName

data class InternetRadioDto(
    @SerializedName("changeuuid")
    val changeuuid: String,
    @SerializedName("stationuuid")
    val stationuuid: String,
    @SerializedName("serveruuid")
    val serveruuid: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("url")
    val url: String,
    @SerializedName("url_resolved")
    val url_resolved: String,
    @SerializedName("homepage")
    val homepage: String,
    @SerializedName("favicon")
    val favicon: String,
    @SerializedName("tags")
    val tags: String,
    @SerializedName("country")
    val country: String,
    @SerializedName("countrycode")
    val countrycode: String,
    @SerializedName("iso_3166_2")
    val iso_3166_2: String,
    @SerializedName("state")
    val state: String,
    @SerializedName("language")
    val language: String,
    @SerializedName("languagecodes")
    val languagecodes: String,
    @SerializedName("votes")
    val votes: Int,
    @SerializedName("lastchangetime")
    val lastchangetime: String,
    @SerializedName("lastchangetime_iso8601")
    val lastchangetime_iso8601: String,
    @SerializedName("codec")
    val codec: String,
    @SerializedName("bitrate")
    val bitrate: Int,
    @SerializedName("hls")
    val hls: Int,
    @SerializedName("lastcheckok")
    val lastcheckok: Int,
    @SerializedName("lastchecktime")
    val lastchecktime: String,
    @SerializedName("lastchecktime_iso8601")
    val lastchecktime_iso8601: String,
    @SerializedName("lastcheckoktime")
    val lastcheckoktime: String,
    @SerializedName("lastcheckoktime_iso8601")
    val lastcheckoktime_iso8601: String
)