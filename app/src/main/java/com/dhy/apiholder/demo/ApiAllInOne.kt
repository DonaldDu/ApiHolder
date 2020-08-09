package com.dhy.apiholder.demo

import io.reactivex.rxjava3.core.Observable
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiAllInOne {
    /************************** server A ****************************/
    @GET("user/loginWithScanCode")
    fun aMethod1(@Query("id") id: Int): Observable<ResponseBody>

    /************************** server B ****************************/
    @GET("user/loginWithScanCode")
    fun bMethod1(@Query("id") id: Int): Observable<ResponseBody>
}

const val SERVER_HOST_A = "https://www.a.com/"
const val SERVER_HOST_B = "https://www.b.com/"
fun getApi(retrofit: Retrofit, host: String): ApiAllInOne {
    return retrofit.newBuilder()
            .baseUrl(host).build()
            .create(ApiAllInOne::class.java)
}

fun showNomalUseCase(retrofit: Retrofit) {
    val apiA = getApi(retrofit, SERVER_HOST_A)//save as single instance for repeated usage
    apiA.aMethod1(1).subscribe()
    apiA.bMethod1(1).subscribe()//invalid usage, but no compile error

    val apiB = getApi(retrofit, SERVER_HOST_B)
    apiB.bMethod1(1).subscribe()
    apiB.aMethod1(1).subscribe()//invalid usage, but no compile error
}