# ApiHolder [![](https://jitpack.io/v/DonaldDu/ApiHolder.svg)](https://jitpack.io/#DonaldDu/ApiHolder)
```
    private fun initHoderApi() {
        val util = ApiHolderUtil()
        val api = util.createHolderApi(ApiHolder::class.java)
        api.methodA(1)
        api.methodB(1)
        api.methodC(1)

        //update api when needed
        util.updateApi(ApiA::class.java, "https://www.a.com/")
    }

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
