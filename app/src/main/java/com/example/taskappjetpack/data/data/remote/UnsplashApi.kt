package com.example.taskappjetpack.data.data.remote


import com.example.taskappjetpack.BuildConfig
import com.example.taskappjetpack.BuildConfig.*
import com.example.taskappjetpack.model.UnSplashImage
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface UnsplashApi {

    @Headers("Authorization: Client-ID ${BuildConfig.API_KEY}")
    @GET("/photos")
    suspend fun getAllImages(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): List<UnSplashImage>

    @Headers("Authorization: Client-ID ${BuildConfig.API_KEY}")
    @GET("/search/photos")
    suspend fun searchAllImages(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): List<UnSplashImage>
}