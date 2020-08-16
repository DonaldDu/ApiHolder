package com.dhy.apiholder.demo

import com.dhy.apiholder.ApiHolderUtil
import com.dhy.apiholder.BaseUrlData
import io.reactivex.rxjava3.core.Observable
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiHolderE : AppApi, PushApi

@BaseUrl(ServerConfig.APP)
interface AppApi {
    @GET("user/loginWithScanCode")
    fun login(@Query("id") id: Int): Observable<ResponseBody>
}

@BaseUrl(ServerConfig.PUSH)
interface PushApi {
    @GET("user/loginWithScanCode")
    fun push(@Query("id") id: Int): Observable<ResponseBody>
}

class ApiUtilE : ApiHolderUtil<ApiHolderE>(ApiHolderE::class) {
    override fun getUserBaseUrl(cls: Class<*>): BaseUrlData {
        val baseUrl = cls.getAnnotation(BaseUrl::class.java)
        return if (baseUrl != null) BaseUrlData(baseUrl.value.release, baseUrl.append)
        else {
            throw IllegalArgumentException(String.format("%s: MUST ANNOTATE WITH 'BaseUrl'", cls.name))
        }
    }
}

@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class BaseUrl(
        val value: ServerConfig,
        /**
         * append to value, auto check separator of '/'
         */
        val append: String = ""
)