package com.example.glancebetasample

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query

interface QiitaApiClient {
    @GET("/api/v2/items")
    suspend fun getArticles(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20,
        @Query("query") query: String = "Android"
    ): Response<List<ArticleResponse>>

    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        fun provideQiitaApiClient(): QiitaApiClient {
            val contentType = "application/json".toMediaType()
            val json = Json {
                ignoreUnknownKeys = true
                explicitNulls = false
            }
            val interceptor = HttpLoggingInterceptor().also {
                it.level = HttpLoggingInterceptor.Level.BODY
            }
            val okhttpClient=  OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build()
            val retrofit = Retrofit.Builder()
                .baseUrl("https://qiita.com/api/v2/")
                .addConverterFactory(json.asConverterFactory(contentType))
                .client(okhttpClient)
                .build()
            return retrofit.create(QiitaApiClient::class.java)
        }
    }
}