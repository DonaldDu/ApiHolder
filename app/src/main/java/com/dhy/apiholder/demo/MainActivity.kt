package com.dhy.apiholder.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dhy.apiholder.ApiHolderUtil

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initHoderApi()
    }

    private fun initHoderApi() {
        val util = ApiHolderUtil(ApiHolder::class)
        val api = util.api
        api.methodA(1)
        api.methodB(1)
        api.methodC(1)

        //update api when needed
        util.updateApi(ApiA::class, "https://www.a2.com/")
    }
}
