package com.dhy.apiholder.demo

import android.database.Observable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import com.dhy.apiholder.ApiHolderUtil
import com.dhy.apiholder.BaseUrl

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initHoderApi()
    }

    private fun initHoderApi() {
        val util = ApiHolderUtil()
        val api = util.createHolderApi(ApiHolder::class.java)
        api.methodA(1)
        api.methodB(1)
        api.methodC(1)

        //update api when needed
        util.updateApi(ApiA::class.java, "https://www.a.com/")
    }

    interface ApiHolder : ApiA, ApiB, ApiC

    @BaseUrl("https://www.a.com/")
    interface ApiA {
        @GET("user/loginWithScanCode")
        fun methodA(@Query("id") id: Int): Observable<ResponseBody>
    }

    @BaseUrl("https://www.b.com/")
    interface ApiB {
        @GET("user/loginWithScanCode")
        fun methodB(@Query("id") id: Int): Observable<ResponseBody>
    }

    @BaseUrl("https://www.c.com/")
    interface ApiC {
        @GET("user/loginWithScanCode")
        fun methodC(@Query("id") id: Int): Observable<ResponseBody>
    }
}
