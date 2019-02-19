package com.dhy.apiholder

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import kotlin.reflect.KClass

open class ApiHolderUtil<HOLDER : Any>(private val holder: KClass<HOLDER>) {
    private lateinit var retrofit: Retrofit
    private val apis: MutableMap<Class<*>, Any> = mutableMapOf()
    private val baseUrls: MutableMap<Class<*>, String> = mutableMapOf()
    private var autoCmdUtil: AutoCmdUtil? = null
    private lateinit var client: OkHttpClient

    val api: HOLDER by lazy { createHolderApi(holder) }

    protected open fun validateEagerly(): Boolean = false

    protected open fun getClient(): OkHttpClient = OkHttpClient()
    protected open fun getGsonConverterFactory(): GsonConverterFactory = GsonConverterFactory.create()
    protected open fun getRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
                .validateEagerly(validateEagerly())
                .addConverterFactory(getGsonConverterFactory())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl("http://www.demo.com/")
                .client(client)
                .build()
    }

    private fun init() {
        this.client = getClient()
        this.retrofit = getRetrofit(client)
        if (validateEagerly()) {
            try {
                autoCmdUtil = AutoCmdUtil()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun createHolderApi(apiHolder: KClass<HOLDER>): HOLDER {
        init()
        val partApis = apiHolder.java.interfaces
        for (api in partApis) {
            updateApi(api)
        }
        return createHolderApi(apis, apiHolder.java)
    }

    fun updateApi(releaseDomain: String, newDomain: String) {
        val interfaces = holder.java.interfaces
        for (api in interfaces) {
            val baseUrl = api.getAnnotation(BaseUrl::class.java)!!
            if (baseUrl.value == releaseDomain) {
                updateApi(api, newDomain)
            }
        }
    }

    fun updateApi(apiClass: KClass<*>, newDomain: String? = null) {
        updateApi(apiClass.java, newDomain)
    }

    private fun updateApi(apiClass: Class<*>, newDomain: String? = null) {
        val retrofitBuilder = retrofit.newBuilder()
        initTimeout(retrofitBuilder, apiClass)
        val url = getBaseUrl(apiClass, newDomain)
        val retrofit = retrofitBuilder.baseUrl(url).build()
        apis[apiClass] = retrofit.create(apiClass) as Any
        baseUrls[apiClass] = url

        if (validateEagerly() && autoCmdUtil != null) {
            try {
                autoCmdUtil!!.initHeaders(retrofit)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun initTimeout(retrofitBuilder: Retrofit.Builder, apiClass: Class<*>) {
        if (apiClass.isAnnotationPresent(Timeout::class.java)) {
            val timeout = apiClass.getAnnotation<Timeout>(Timeout::class.java)
            val builder = client.newBuilder()
            if (timeout.connect > 0) {
                builder.connectTimeout(timeout.connect, timeout.timeUnit)
            }
            if (timeout.read > 0) {
                builder.readTimeout(timeout.read, timeout.timeUnit)
            }
            if (timeout.write > 0) {
                builder.writeTimeout(timeout.write, timeout.timeUnit)
            }
            retrofitBuilder.client(builder.build())
        }
    }

    fun getUsingBaseUrl(api: KClass<*>): String {
        return baseUrls[api.java]!!
    }

    /**
     * eg: BaseUrl(value="https://www.a.com/", append = "apiA") => "https://www.a.com"
     *
     * @return using url without append & '/'
     */
    fun getUsingDomain(api: KClass<*>): String {
        val cls = api.java
        if (cls.isAnnotationPresent(BaseUrl::class.java)) {
            val baseUrl = cls.getAnnotation<BaseUrl>(BaseUrl::class.java)
            var append = baseUrl.append
            var url = getUsingBaseUrl(api)
            url = url.trim("/")
            return if (append.isEmpty()) {
                url
            } else {
                append = append.trim("/")
                url = url.trim(append)
                url.trim("/")
            }
        } else {
            throw IllegalArgumentException(String.format("%s: MUST ANNOTATE WITH '%s'", cls.name, BaseUrl::class.java.name))
        }
    }

    /**
     * hold all api in one
     */
    @Suppress("UNCHECKED_CAST")
    private fun createHolderApi(apis: Map<Class<*>, Any>, apiHolder: Class<HOLDER>): HOLDER {
        return Proxy.newProxyInstance(apiHolder.classLoader, arrayOf<Class<*>>(apiHolder)) { _, method, args ->
            val api = apis[method.declaringClass]!!
            invokeApi(api, method, args)
        } as HOLDER
    }

    @Throws(Throwable::class)
    protected open fun invokeApi(api: Any, method: Method, args: Array<*>?): Any {
        return if (args != null) method.invoke(api, *args)
        else method.invoke(api)
    }

    companion object {
        /**
         * get full url with append, end with "/"
         */
        @JvmStatic
        fun getBaseUrl(api: KClass<*>, newDomain: String? = null): String {
            return getBaseUrl(api.java, newDomain)
        }

        private fun getBaseUrl(api: Class<*>, newDomain: String? = null): String {
            if (api.isAnnotationPresent(BaseUrl::class.java)) {
                val baseUrl = api.getAnnotation<BaseUrl>(BaseUrl::class.java)
                var url = newDomain ?: baseUrl.value
                if (!url.endsWith("/")) url += "/"

                var append = baseUrl.append
                if (append.isNotEmpty()) {
                    if (append.startsWith("/")) append = append.substring(1)
                    url += append
                }
                if (!url.endsWith("/")) url += "/"
                return url
            } else {
                throw IllegalArgumentException(String.format("%s: MUST ANNOTATE WITH '%s'", api.name, BaseUrl::class.java.name))
            }
        }
    }
}

fun String.trim(tail: String): String {
    return if (endsWith(tail)) {
        substring(0, length - tail.length)
    } else this
}