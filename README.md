# ApiHolder（聚合API） [![](https://jitpack.io/v/DonaldDu/ApiHolder.svg)](https://jitpack.io/#DonaldDu/ApiHolder)
## 功能
1. 聚合所有接口到一个类，再也不用为区分接口在哪个类而烦恼了！
2. 给接口设置默认AndroidSchedulers，不用每次都写。
## 使用方法
```
    private fun initHoderApi() {
        val util = ApiHolderUtil()
        val api = util.createHolderApi(ApiHolder::class.java)
        api.methodA(1)
        api.methodB(1)
        api.methodC(1)

        //update api when needed
        util.updateApi(ApiA::class.java, "https://www.a2.com/")
    }

```
```
dependencies {
    implementation 'com.github.DonaldDu:ApiHolder:x.x.x'
}
```
## 定义接口
在一个类中定义所有接口，方便查找和维护
```
    interface ApiHolder : ApiA, ApiB, ApiC

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

    @BaseUrl("https://www.c.com/")
    interface ApiC {
        @GET("user/loginWithScanCode")
        fun methodC(@Query("id") id: Int): Observable<ResponseBody>
    }
```
