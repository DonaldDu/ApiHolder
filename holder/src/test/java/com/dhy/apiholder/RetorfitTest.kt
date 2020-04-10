package com.dhy.apiholder

import io.reactivex.Observable
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url

class RetorfitTest {
    @Test
    fun test() {
        val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl("http://www.demo.com/")
                .build()
        val api = retrofit.create(APIs::class.java)

        api.fetchHTML(false).subscribe()
    }
}


interface APIs {

    @GET("https://www.baidu.com/")
    fun fetchHTML(@GET2 unused: Boolean): Observable<String>
}


@Target(AnnotationTarget.VALUE_PARAMETER)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class GET2(
        /**
         * A relative or absolute path, or full URL of the endpoint. This value is optional if the first
         * parameter of the method is annotated with [@Url][Url].
         *
         *
         * See [base URL][retrofit2.Retrofit.Builder.baseUrl] for details of how
         * this is resolved against a base URL to create the full endpoint URL.
         */
        val value: String = "")