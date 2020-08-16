package com.dhy.apiholder

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.net.URL
import kotlin.reflect.KClass

open class ApiHolderUtil<HOLDER : Any>(private val holder: KClass<HOLDER>, private val validateEagerly: Boolean = false) {
    val api: HOLDER by lazy { createHolderApi(holder) }
    private val okHttpClient by lazy { getClient() }
    private val retrofit by lazy { getRetrofit(okHttpClient) }
    private val apis: MutableMap<Class<*>, Any> = mutableMapOf()
    private val baseUrls: MutableMap<Class<*>, String> = mutableMapOf()
    protected open fun getClient(): OkHttpClient = OkHttpClient()
    protected open fun getGsonConverterFactory(): GsonConverterFactory = GsonConverterFactory.create()
    protected open fun getRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
                .validateEagerly(validateEagerly)
                .addConverterFactory(getGsonConverterFactory())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .baseUrl("http://www.demo.com/")
                .client(client)
                .build()
    }

    private fun createHolderApi(apiHolder: KClass<HOLDER>): HOLDER {
        val partApis = apiHolder.java.interfaces
        for (apiClass in partApis) {
            val api = apis[apiClass]
            if (api == null) updateApi(apiClass)
        }
        onCreateHolderApi()
        return createHolderApi(apis, apiHolder.java)
    }

    open fun onCreateHolderApi() {

    }

    /**
     * the domain end which is '/' will be ignored
     * */
    fun updateApi(releaseDomain: String, newDomain: String) {
        val release = releaseDomain.appendPath()
        val apis = holder.java.interfaces.filter { getAnnotationUrl(it).value.appendPath() == release }
        if (apis.isNotEmpty()) {
            apis.forEach {
                updateApi(it, newDomain)
            }
        }
    }

    fun updateApi(apiClass: KClass<*>, newDomain: String? = null) {
        updateApi(apiClass.java, newDomain)
    }

    private fun updateApi(apiClass: Class<*>, newDomain: String? = null) {
        val retrofitBuilder = retrofit.newBuilder()
        retrofitBuilder.initTimeout(apiClass)
        val url = getBaseUrl(apiClass, newDomain).toString()
        val retrofit = retrofitBuilder.baseUrl(url).build()
        apis[apiClass] = retrofit.create(apiClass) as Any
        baseUrls[apiClass] = url
    }

    private fun Retrofit.Builder.initTimeout(apiClass: Class<*>) {
        if (apiClass.isAnnotationPresent(Timeout::class.java)) {
            val timeout = apiClass.getAnnotation(Timeout::class.java)!!
            val builder = okHttpClient.newBuilder()
            if (timeout.connect > 0) {
                builder.connectTimeout(timeout.connect, timeout.timeUnit)
            }
            if (timeout.read > 0) {
                builder.readTimeout(timeout.read, timeout.timeUnit)
            }
            if (timeout.write > 0) {
                builder.writeTimeout(timeout.write, timeout.timeUnit)
            }
            client(builder.build())
        }
    }

    /**
     * hold all api in one
     */
    @Suppress("UNCHECKED_CAST")
    private fun createHolderApi(apis: Map<Class<*>, Any>, apiHolder: Class<HOLDER>): HOLDER {
        return Proxy.newProxyInstance(apiHolder.classLoader, arrayOf<Class<*>>(apiHolder)) { _, method, args ->
            val api = apis.getValue(method.declaringClass)
            invokeApi(api, method, args)
        } as HOLDER
    }

    @Throws(Throwable::class)
    protected open fun invokeApi(api: Any, method: Method, args: Array<*>?): Any {
        return if (args != null) method.invoke(api, *args)
        else method.invoke(api)
    }

    /**
     * eg: BaseUrl(value="domain", append = "a") => "domain"
     *
     * @return using domain without append & '/'
     */
    fun getUsingDomain(api: KClass<*>): String {
        val cls = api.java
        val url = getBaseUrl(cls.kotlin, false).toString()
        val append = getAnnotationUrl(cls).append
        return url.replace("/?$append/?\$".toRegex(), "")
    }

    /**
     * @param release release or current using full url
     * */
    fun getBaseUrl(api: KClass<*>, release: Boolean): URL {
        return if (baseUrls.isEmpty() || release) getBaseUrl(api.java)
        else {
            val url = baseUrls[api.java]
            if (url != null) URL(url)
            else getBaseUrl(api.java)
        }
    }

    /**
     * get full url with append, end with "/"
     */

    fun getBaseUrl(api: KClass<*>, newDomain: String? = null): URL {
        return getBaseUrl(api.java, newDomain)
    }

    /**
     * get full url with append, end with "/"
     */

    fun getBaseUrl(api: Class<*>, newDomain: String? = null): URL {
        val baseUrl = getAnnotationUrl(api)
        val domain = newDomain ?: baseUrl.value
        return URL(domain.appendPath(baseUrl.append))
    }

    private val annotations: MutableMap<Class<*>, BaseUrlData> = mutableMapOf()
    private fun getAnnotationUrl(cls: Class<*>, annotationBuffer: MutableMap<Class<*>, BaseUrlData> = annotations): BaseUrlData {
        val buffer = annotationBuffer[cls]
        return if (buffer != null) buffer
        else {
            val baseUrl = if (cls.isAnnotationPresent(BaseUrl::class.java)) {
                val data = cls.getAnnotation(BaseUrl::class.java)!!
                BaseUrlData(data.value, data.append)
            } else {
                getUserBaseUrl(cls)
            }
            annotationBuffer[cls] = baseUrl
            baseUrl
        }
    }

    open fun getUserBaseUrl(cls: Class<*>): BaseUrlData {
        throw IllegalArgumentException("not supported yet")
    }
}

data class BaseUrlData(val value: String, val append: String)

fun String.trim(tail: String): String {
    return if (endsWith(tail)) {
        substring(0, length - tail.length)
    } else this
}

/**
 * appendPath for URL
 * @return end with "/"
 * */
fun String.appendPath(path: String? = null): String {
    val url = this + "/" + (path ?: "") + "/"
    return url.replace("([^:/])/{2,}".toRegex(), "$1/")
}