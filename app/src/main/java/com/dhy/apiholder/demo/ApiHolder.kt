package com.dhy.apiholder.demo

import com.dhy.apiholder.ApiHolderUtil
import com.dhy.apiholder.BaseUrl
import com.dhy.apiholder.Timeout
import io.reactivex.rxjava3.core.Observable
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface ApiHolder : ApiA, ApiB

@BaseUrl("https://www.a.com/", append = "apiA")
interface ApiA {
    @GET("user/loginWithScanCode")
    fun methodA(@Query("id") id: Int): Observable<ResponseBody>
}

@BaseUrl("https://www.b.com/")
@Timeout(read = 100, timeUnit = TimeUnit.SECONDS)
interface ApiB {
    @GET("user/loginWithScanCode")
    fun methodB(@Query("id") id: Int): Observable<ResponseBody>
}

fun showApiHolderUsage() {
    val api = ApiHolderUtil(ApiHolder::class).api
    api.methodA(1).subscribe()
    api.methodB(1).subscribe()
}