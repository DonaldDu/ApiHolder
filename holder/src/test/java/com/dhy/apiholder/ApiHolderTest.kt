package com.dhy.apiholder

import io.reactivex.rxjava3.core.Observable
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

class ApiHolderTest {
    private val holder = ApiHolderUtil(ApiHolder::class, true)

    @Test
    fun testMe() {
        val api = holder.api
        api.methodA(1)
        assertTrue(holder.isRelease())//must after api called
        holder.updateApi(DOMAIN_TESTD, "https://www.d1.com/")
        holder.updateApi(DOMAIN_TESTD, "https://www.d1.com")

        holder.updateApi("$DOMAIN_TESTD/", "https://www.d1.com/")
        holder.updateApi("$DOMAIN_TESTD/", "https://www.d1.com")

        api.methodA(1)
        api.methodB(1)
        api.methodC(1)
    }

    @Test
    fun errorRelease() {
        val error = try {
            holder.updateApi(DOMAIN_TESTD + "abc", "https://www.d1.com/")
            false
        } catch (e: Exception) {
            true
        }
        assertEquals("errorRelease should be error", false, error)
    }

    @Test
    fun getUsingDomain() {
        val domain = "https://www.domain.com/"
        testUsingDomain(domain, "")
        testUsingDomain(domain, "a")
        testUsingDomain(domain, "a/")
        testUsingDomain(domain, "/a")
        testUsingDomain(domain, "/a/")
    }

    @Test
    fun showBaseUrl() {
        val baseUrl = ApiA::class.java.getAnnotation(BaseUrl::class.java)!!
        println(baseUrl.value)
    }

    private fun testUsingDomain(domain: String, append: String) {
        val url = domain.appendPath(append)
        val reg = "/?$append/?\$".toRegex()
        assertEquals("append:'$append'", domain.trim("/"), url.replace(reg, ""))
    }
}

interface ApiHolder : ApiA, ApiB, ApiC, ApiD

const val DOMAIN_TESTD = "https://www.d.com"

@BaseUrl("https://www.a.com/", append = "apiA", rootApi = true)
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

@BaseUrl(DOMAIN_TESTD)
@Timeout(read = 100)
interface ApiD {
    @GET("user/loginWithScanCode")
    fun methodD(@Query("id") id: Int): Observable<ResponseBody>
}