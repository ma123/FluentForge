package com.identic.fluentforge.data.remote.repository

import com.identic.fluentforge.common.Resource
import com.identic.fluentforge.domain.model.InternetRadio
import kotlinx.coroutines.flow.Flow

interface InternetRadioRepository {
    suspend fun getAllInternetRadios():Flow<Resource<List<InternetRadio>>>
}