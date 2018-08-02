# ApiHolder[![](https://jitpack.io/v/DonaldDu/ApiHolder.svg)](https://jitpack.io/#DonaldDu/ApiHolder)
```
    private void initApiHoder() {
        ApiHolderUtil util = new ApiHolderUtil(null, null);
        List<PartApi> parts = new ArrayList<>();
        parts.add(new PartApi(ApiA.class, "https://www.a.com/"));
        parts.add(new PartApi(ApiB.class, "https://www.b.com/"));
        parts.add(new PartApi(ApiC.class, "https://www.c.com/"));

        ApiHolder api = util.createHolderApi(ApiHolder.class, parts, true);
        api.methodA(1);
        api.methodB(1);
        api.methodC(1);
    }

    private interface ApiHolder extends ApiA, ApiB, ApiC {

    }

    private interface ApiA {
        @GET("user/loginWithScanCode")
        Observable<ResponseBody> methodA(@Query("id") int id);
    }

    private interface ApiB {
        @GET("user/loginWithScanCode")
        Observable<ResponseBody> methodB(@Query("id") int id);
    }

    private interface ApiC {
        @GET("user/loginWithScanCode")
        Observable<ResponseBody> methodC(@Query("id") int id);
    }
```
