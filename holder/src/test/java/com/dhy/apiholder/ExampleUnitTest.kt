package com.dhy.apiholder

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class ExampleUnitTest {
    @Test
    fun appendPathTest() {
        val url = "https://www.baidu.com/456654/"
        assertEquals(url, "https://www.baidu.com/".appendPath("456654"))
        assertEquals(url, "https://www.baidu.com/".appendPath("/456654"))

        assertEquals(url, "https://www.baidu.com".appendPath("456654"))
        assertEquals(url, "https://www.baidu.com".appendPath("/456654"))

        assertEquals(url, "https://www.baidu.com//".appendPath("//456654"))

        assertEquals("file:///android_asset/a/", "file:///android_asset/".appendPath("//a"))
    }
}