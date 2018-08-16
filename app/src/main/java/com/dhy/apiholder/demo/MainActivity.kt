package com.dhy.apiholder.demo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.dhy.apiholder.ApiHolderUtil

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initHoderApi()
    }

    private fun initHoderApi() {
        val util = ApiHolderUtil()
        val api = util.createHolderApi(ApiHolder::class.java)
        api.methodA(1)
        api.methodB(1)
        api.methodC(1)

        //update api when needed
        util.updateApi(ApiA::class.java, "https://www.a2.com/")
    }
}
