# ApiHolder [![](https://jitpack.io/v/DonaldDu/ApiHolder.svg)](https://jitpack.io/#DonaldDu/ApiHolder)
## 功能
1. 聚合所有Retrofit接口到一个类，方便调用。
2. 支持全局给接口设置默认AndroidSchedulers等，不用每次都写。
## 使用方法
```
    private fun initHoderApi() {
        val util = ApiHolderUtil(ApiHolder::class)
        val api = util.api
        api.methodA(1)
        api.methodB(1)

        //update api baseUrl when needed
        util.updateApi(ApiA::class, "https://www.a2.com/")
    }

```
```
dependencies {
    implementation 'com.github.DonaldDu:ApiHolder:x.x.x'//jitpack version
}
```
## 定义接口
在一个类中定义所有Retrofit接口，方便查找和维护
```
    interface ApiHolder : ApiA, ApiB

    @BaseUrl("https://www.a.com/")
    interface ApiA {
        @GET("user/loginWithScanCode")
        fun methodA(@Query("id") id: Int): Observable<ResponseBody>
    }

    @BaseUrl("https://www.b.com/")
    interface ApiB {
        @GET("user/loginWithScanCode")
        fun methodB(@Query("id") id: Int): Observable<ResponseBody>
    }
```
