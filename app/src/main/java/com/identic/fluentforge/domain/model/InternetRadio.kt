package com.identic.fluentforge.domain.model

import androidx.compose.runtime.Immutable
import java.util.UUID

@Immutable
data class InternetRadio(
    val changeuuid: String? = null,
    val stationuuid: String? = null,
    val serveruuid: String? = null,
    val name: String? = null,
    val url: String? = null,
    val urlResolved: String? = null,
    val homepage: String? = null,
    val favicon: String? = null,
    val tags: String? = null,
    val country: String? = null,
    val countrycode: String? = null,
    val iso31662: String? = null,
    val state: String? = null,
    val language: String? = null,
    val languagecodes: String? = null,
    val votes: Int? = null,
    val lastchangetime: String? = null,
    val lastchangetimeIso8601: String? = null,
    val codec: String? = null,
    val bitrate: Int? = null,
    val hls: Int? = null,
    val lastcheckok: Int? = null,
    val lastchecktime: String? = null,
    val lastchecktimeIso8601: String? = null,
    val lastcheckoktime: String? = null,
    val lastcheckoktimeIso8601: String? = null,
    val id: String = UUID.randomUUID().toString()
)
