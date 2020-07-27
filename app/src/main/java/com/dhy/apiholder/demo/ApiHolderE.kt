package com.dhy.apiholder.demo

import com.dhy.apiholder.ApiHolderUtil
import com.dhy.apiholder.BaseUrlData
import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiHolderE : AppApi

@BaseUrl(ServerConfig.APP)
interface AppApi {
    @GET("user/loginWithScanCode")
    fun methodA(@Query("id") id: Int): Observable<ResponseBody>
}

@BaseUrl(ServerConfig.PUSH)
interface PushApi {
    @GET("user/loginWithScanCode")
    fun methodA(@Query("id") id: Int): Observable<ResponseBody>
}

class ApiUtil : ApiHolderUtil<ApiHolderE>(ApiHolderE::class) {
    override fun getEnumBaseUrl(cls: Class<*>): BaseUrlData {
        val baseUrl = cls.getAnnotation(BaseUrl::class.java)
        return if (baseUrl != null) BaseUrlData(baseUrl.value.release, baseUrl.append, baseUrl.rootApi)
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
        val append: String = "",
        /**
         * marke as root api for  [ApiHolderUtil.isRelease]
         */
        val rootApi: Boolean = false
)