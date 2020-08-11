package com.dhy.apiholder.demo

import io.reactivex.rxjava3.core.Observable
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface ApiHeaderCase {
    /************************** server A ****************************/
    @Headers("host:$SERVER_HOST_A")
    @GET("user/loginWithScanCode")
    fun aMethod1(@Query("id") id: Int): Observable<ResponseBody>

    /************************** server B ****************************/
    @Headers("host:$SERVER_HOST_B")
    @GET("user/loginWithScanCode")
    fun bMethod1(@Query("id") id: Int): Observable<ResponseBody>
}