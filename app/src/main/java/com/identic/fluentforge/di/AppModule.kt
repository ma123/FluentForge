package com.identic.fluentforge.di

import com.identic.fluentforge.common.Constants
import com.identic.fluentforge.data.InternetRadioApi
import com.identic.fluentforge.data.remote.repository.InternetRadioRepository
import com.identic.fluentforge.domain.repository.InternetRadioRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideInternetRadioApi(): InternetRadioApi {
        return Retrofit.Builder()
            .baseUrl(Constants.INTERNET_RADIO_URL)
            .addConverterFactory(GsonConverterFactory.create()).build()
            .create(InternetRadioApi::class.java)
    }

    @Provides
    @Singleton
    fun provideInternetRadioRepository(api: InternetRadioApi): InternetRadioRepository {
        return InternetRadioRepositoryImpl(api)
    }
}