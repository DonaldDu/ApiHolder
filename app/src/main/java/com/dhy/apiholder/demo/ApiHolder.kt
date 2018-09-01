package com.dhy.apiholder.demo

import com.dhy.apiholder.BaseUrl
import com.dhy.apiholder.Timeout
import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface ApiHolder : ApiA, ApiB, ApiC

@BaseUrl("https://www.a.com/")
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
interface Apid {
    @GET("user/loginWithScanCode")
    fun methodd(@Query("id") id: Int): Observable<ResponseBody>
}