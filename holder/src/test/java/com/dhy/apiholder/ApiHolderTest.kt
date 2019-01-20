package com.dhy.apiholder

import org.junit.Test

class ApiHolderTest {
    @Test
    fun testMe() {
        val api = ApiHolderUtil(ApiHolder::class).api
        api.methodA(1).subscribe()
    }
}