package com.dhy.apiholder.demo

import com.dhy.apiholder.ApiHolderUtil
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.lang.reflect.Method

class ApiUtil : ApiHolderUtil<ApiHolder>(ApiHolder::class) {
    companion object {
        val apiUtil = ApiUtil()
        val api = apiUtil.api
    }

    override fun invokeApi(api: Any, method: Method, args: Array<*>?): Any {
        val observable = super.invokeApi(api, method, args) as Observable<*>
        return observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }
}