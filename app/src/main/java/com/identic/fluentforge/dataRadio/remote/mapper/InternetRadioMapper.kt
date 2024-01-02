package com.identic.fluentforge.dataRadio.remote.mapper

import com.identic.fluentforge.dataRadio.remote.dto.InternetRadioDto
import com.identic.fluentforge.domain.model.InternetRadio

fun InternetRadioDto.toInternetRadio(): InternetRadio {
    return InternetRadio(
        changeuuid = changeuuid,
        stationuuid = stationuuid,
        serveruuid = serveruuid,
        name = name,
        url = url,
        urlResolved = url_resolved,
        homepage = homepage,
        favicon = favicon,
        tags = tags,
        country = country,
        countrycode = countrycode,
        iso31662 = iso_3166_2,
        state = state,
        language = language,
        languagecodes = languagecodes,
        votes = votes,
        lastchangetime = lastchangetime,
        lastchangetimeIso8601 = lastchangetime_iso8601,
        codec = codec,
        bitrate = bitrate,
        hls = hls,
        lastcheckok = lastcheckok,
        lastchecktime = lastchecktime,
        lastchecktimeIso8601 = lastcheckoktime_iso8601,
        lastcheckoktime = lastcheckoktime,
        lastcheckoktimeIso8601 = lastcheckoktime_iso8601
    )
}