package com.example.custom_gesture_lib.version

import org.telegram.version.VersionData
import retrofit2.Call
import retrofit2.http.GET


interface  ApiService {
    @GET("version.txt")
    fun getVersionData(): Call<VersionData>
}