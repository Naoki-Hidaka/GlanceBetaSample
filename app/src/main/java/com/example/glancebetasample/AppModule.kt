package com.example.glancebetasample

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {
    @OptIn(ExperimentalSerializationApi::class)
    @Provides
    @Singleton
    fun provideQiitaApiClient(): QiitaApiClient {
        val contentType = "application/json".toMediaType()
        val json = Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
        val retrofit = Retrofit.Builder()
            .baseUrl("https://qiita.com/apiv2")
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
        return retrofit.create(QiitaApiClient::class.java)
    }
}