package com.dhy.apiholder.demo

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dhy.apiholder.demo.ApiUtil.Companion.api
import com.dhy.apiholder.demo.ApiUtil.Companion.apiUtil
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btA.setOnClickListener {
            api.methodA(1).subscribe({
                showMsg("onNext")
            }, {
                it.printStackTrace()
                showMsg(it.message ?: "empty error")
            }, {
                showMsg("onComplete")
            })
        }
        btB.setOnClickListener {
            api.methodB(1).subscribe({
                showMsg("onNext")
            }, {
                it.printStackTrace()
                showMsg(it.message ?: "empty error")
            })
        }
        clearLog.setOnClickListener {
            tvLog.text = ""
        }
        //update api when needed
        apiUtil.updateApi(ApiA::class, "https://www.testA.com/")
    }

    @SuppressLint("SetTextI18n")
    fun showMsg(msg: String) {
        tvLog.text = "${tvLog.text}\n$msg"
    }
}
