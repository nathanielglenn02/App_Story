package com.example.app_story.network

import com.example.app_story.model.LoginResponse
import com.example.app_story.model.RegisterResponse
import com.example.app_story.model.StoryResponse
import com.example.app_story.model.UploadResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @FormUrlEncoded
    @POST("register")
    fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<RegisterResponse>

    @FormUrlEncoded
    @POST("login")
    fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<LoginResponse>

    // Endpoint untuk mendapatkan daftar cerita
    @GET("stories")
    fun getStories(
        @Header("Authorization") token: String // Header untuk autentikasi
    ): Call<StoryResponse>

    // Endpoint untuk meng-upload cerita (deskripsi dan foto)
    @Multipart
    @POST("stories")
    fun addStory(
        @Header("Authorization") token: String, // Token autentikasi
        @Part("description") description: RequestBody, // Deskripsi cerita
        @Part photo: MultipartBody.Part, // Foto cerita
    ): Call<UploadResponse>
}
