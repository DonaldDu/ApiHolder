package com.dhy.apiholder

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.net.URL
import kotlin.reflect.KClass

open class ApiHolderUtil<HOLDER : Any>(private val holder: KClass<HOLDER>, private val validateEagerly: Boolean = false) {
    private val mRetrofit: Retrofit by lazy { getRetrofit(okHttpClient) }
    private val apis: MutableMap<Class<*>, Any> = mutableMapOf()
    private val baseUrls: MutableMap<Class<*>, String> = mutableMapOf()
    private val autoCmdUtil: AutoCmdUtil? by lazy {
        if (validateEagerly) {
            return@lazy try {
                AutoCmdUtil()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } else null
    }
    private val okHttpClient: OkHttpClient by lazy { getClient() }
    val api: HOLDER by lazy { createHolderApi(holder) }

    protected open fun getClient(): OkHttpClient = OkHttpClient()
    protected open fun getGsonConverterFactory(): GsonConverterFactory = GsonConverterFactory.create()
    protected open fun getRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
                .validateEagerly(validateEagerly)
                .addConverterFactory(getGsonConverterFactory())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl("http://www.demo.com/")
                .client(client)
                .build()
    }

    private fun createHolderApi(apiHolder: KClass<HOLDER>): HOLDER {
        val partApis = apiHolder.java.interfaces
        for (api in partApis) {
            updateApi(api)
        }
        return createHolderApi(apis, apiHolder.java)
    }

    /**
     * the domain end which is '/' will be ignored
     * */
    fun updateApi(releaseDomain: String, newDomain: String) {
        val release = releaseDomain.appendPath()
        val apis = holder.java.interfaces.filter { it.getBaseUrl()!!.value.appendPath() == release }
        if (apis.isNotEmpty()) {
            apis.forEach {
                updateApi(it, newDomain)
            }
        } else throw  IllegalArgumentException("not api found for domian:$releaseDomain")
    }

    fun updateApi(apiClass: KClass<*>, newDomain: String? = null) {
        updateApi(apiClass.java, newDomain)
    }

    private fun updateApi(apiClass: Class<*>, newDomain: String? = null) {
        val retrofitBuilder = mRetrofit.newBuilder()
        initTimeout(retrofitBuilder, apiClass)
        val url = getBaseUrl(apiClass, newDomain).toString()
        val retrofit = retrofitBuilder.baseUrl(url).build()
        apis[apiClass] = retrofit.create(apiClass) as Any
        baseUrls[apiClass] = url

        if (validateEagerly && autoCmdUtil != null) {
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
            retrofitBuilder.client(builder.build())
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
        if (cls.isAnnotationPresent(BaseUrl::class.java)) {
            val url = getBaseUrl(cls.kotlin, false).toString()
            val append = cls.getBaseUrl()!!.append
            return url.replace("/?$append/?\$".toRegex(), "")
        } else {
            throw IllegalArgumentException(String.format("%s: MUST ANNOTATE WITH '%s'", cls.name, BaseUrl::class.java.name))
        }
    }

    /**
     * @param release release or current using full url
     * */
    fun getBaseUrl(api: KClass<*>, release: Boolean): URL {
        if (baseUrls.isEmpty()) throw IllegalStateException("you should call ApiHolderUtil.api before ApiHolderUtil.getBaseUrl(api: KClass<*>, release = false) for current baseUrl")
        return if (!release) URL(baseUrls[api.java]!!)
        else getBaseUrl(api.java)
    }

    fun isRelease(): Boolean {
        val rootApi = getRootApi()
        val release = getBaseUrl(rootApi, true)
        val url = getBaseUrl(rootApi, false)
        return url == release
    }

    fun getRootApi(): KClass<*> {
        return holder.java.interfaces.find { it.getBaseUrl()!!.rootApi }?.kotlin
                ?: throw IllegalArgumentException("you should marke one api with com.dhy.apiholder.BaseUrl.rootApi first")
    }

    companion object {


        @JvmStatic
        fun isRelease(apiUtil: ApiHolderUtil<*>): Boolean {
            return isRelease(apiUtil)
        }

        /**
         * get full url with append, end with "/"
         */
        @JvmStatic
        fun getBaseUrl(api: KClass<*>, newDomain: String? = null): URL {
            return getBaseUrl(api.java, newDomain)
        }

        /**
         * get full url with append, end with "/"
         */
        @JvmStatic
        fun getBaseUrl(api: Class<*>, newDomain: String? = null): URL {
            if (api.isAnnotationPresent(BaseUrl::class.java)) {
                val baseUrl = api.getBaseUrl()!!
                val domain = newDomain ?: baseUrl.value
                return URL(domain.appendPath(baseUrl.append))
            } else {
                throw IllegalArgumentException(String.format("%s: MUST ANNOTATE WITH '%s'", api.name, BaseUrl::class.java.name))
            }
        }
    }
}

fun Class<*>.getBaseUrl(): BaseUrl? {
    return getAnnotation(BaseUrl::class.java)
}

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
    return url.replace("([^:])/{2,}".toRegex(), "$1/")
}