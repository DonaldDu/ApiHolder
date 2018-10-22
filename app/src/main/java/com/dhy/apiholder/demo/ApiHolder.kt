package com.dhy.apiholder.demo

import com.dhy.apiholder.BaseUrl
import com.dhy.apiholder.Timeout
import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface ApiHolder : ApiA, ApiB, ApiC, ApiD

@BaseUrl("https://www.a.com/", append = "apiA")
interface ApiA {
    @GET("user/loginWithScanCode")
    fun methodA(@Query("id") id: Int): Observable<ResponseBody>
}

@BaseUrl("https://www.b.com/")
interface ApiB {
    @GET("user/loginWithScanCode")
    @Headers("cmd:test")
    fun methodB(@Query("id") id: Int): Observable<ResponseBody>
}

@BaseUrl("https://www.c.com/")
interface ApiC {
    @GET("user/loginWithScanCode")
    fun methodC(@Query("id") id: Int): Observable<ResponseBody>
}

@BaseUrl("https://www.c.com/")
@Timeout(read = 100)
interface ApiD {
    @GET("user/loginWithScanCode")
    fun methodD(@Query("id") id: Int): Observable<ResponseBody>
}