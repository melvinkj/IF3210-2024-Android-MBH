package com.example.transactionmanagementsystem.api

import com.example.transactionmanagementsystem.models.LoginRequest
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiInterface {
    @Headers("Content-Type:application/json")
    @POST("api/auth/login")
    fun signin(@Body info: LoginRequest): retrofit2.Call<ResponseBody>

    @POST("api/auth/token")
    fun checkToken(@Header("Authorization") token: String): Call<ResponseBody>

    @Multipart
    @POST("api/bill/upload")
    fun uploadBill(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part
    ): Call<ResponseBody>
}

class RetrofitInstance {
    companion object {
        val BASE_URL: String = "https://pbd-backend-2024.vercel.app/"

        val interceptor: HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
            this.level = HttpLoggingInterceptor.Level.BODY
        }

        val client: OkHttpClient = OkHttpClient.Builder().apply {
            this.addInterceptor(interceptor)
        }.build()

        fun getRetrofitInstance(): Retrofit {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
    }
}