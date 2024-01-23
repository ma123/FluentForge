package com.identic.fluentforge.di

import android.content.Context
import com.identic.fluentforge.common.Constants
import com.identic.fluentforge.dataRadio.InternetRadioApi
import com.identic.fluentforge.dataRadio.remote.repository.InternetRadioRepository
import com.identic.fluentforge.domain.repository.InternetRadioRepositoryImpl
import com.identic.fluentforge.reader.database.FluentForgeDatabase
import com.identic.fluentforge.reader.repo.BookRepository
import com.identic.fluentforge.reader.utils.PreferenceUtil
import com.identic.fluentforge.reader.utils.book.BookDownloader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class AppModule {

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

    @Provides
    fun provideAppContext(@ApplicationContext context: Context) = context

    @Singleton
    @Provides
    fun provideFluentForgeDatabase(@ApplicationContext context: Context) =
        FluentForgeDatabase.getInstance(context)

    @Provides
    fun provideLibraryDao(fluentForgeDatabase: FluentForgeDatabase) = fluentForgeDatabase.getLibraryDao()

    @Provides
    fun provideReaderDao(fluentForgeDatabase: FluentForgeDatabase) = fluentForgeDatabase.getReaderDao()

    @Singleton
    @Provides
    fun provideBooksApi() = BookRepository()

    @Singleton
    @Provides
    fun provideBookDownloader(@ApplicationContext context: Context) = BookDownloader(context)

    @Singleton
    @Provides
    fun providePreferenceUtil(@ApplicationContext context: Context) = PreferenceUtil(context)
}