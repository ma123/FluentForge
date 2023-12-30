package com.identic.fluentforge.domain.repository

import com.identic.fluentforge.common.Resource
import com.identic.fluentforge.data.InternetRadioApi
import com.identic.fluentforge.data.remote.mapper.toInternetRadio
import com.identic.fluentforge.data.remote.repository.InternetRadioRepository
import com.identic.fluentforge.domain.model.InternetRadio
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class InternetRadioRepositoryImpl @Inject constructor(
    private val api: InternetRadioApi
) : InternetRadioRepository {
    override suspend fun getAllInternetRadios(): Flow<Resource<List<InternetRadio>>> {
        return flow {
            try {
                emit(Resource.Loading(true))
                val internetRadios = api.getInternetRadiosData()

                emit(Resource.Success(data = internetRadios.map {
                    it.toInternetRadio()
                }))
            } catch (e: HttpException) {
                emit(Resource.Error(e.localizedMessage ?: "An unexpected error occured"))
            } catch (e: IOException) {
                emit(Resource.Error("Couldn't reach server. Check your internet connection."))
            }
        }
    }
}