package com.dhy.apiholder

import org.junit.Test

class ApiHolderTest {
    @Test
    fun testMe() {
        val holder = object : ApiHolderUtil<ApiHolder>(ApiHolder::class) {
            override fun validateEagerly() = true
        }
        val api = holder.api
        api.methodA(1).subscribe()
    }
}