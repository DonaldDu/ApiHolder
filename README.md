# [ApiHolder](https://github.com/DonaldDu/ApiHolder) [![](https://jitpack.io/v/DonaldDu/ApiHolder.svg)](https://jitpack.io/#DonaldDu/ApiHolder)
# 现状
Android项目如果是多服务端接口时，一般怎么弄呢？

#### 方法1：服务器地址放在Header中
把服务器地址放在接口Header中，然后通过拦截器来动态修改请求地址而实现的。除了默认服务器的接口，其它都要加一个Header，有点麻烦。看起来也不爽，不简洁。

```
interface ApiHeaderCase {
    /************************** server A ****************************/
    @Headers("host:$SERVER_HOST_A")
    @GET("user/loginWithScanCode")
    fun aMethod1(@Query("id") id: Int): Observable<ResponseBody>

    /************************** server B ****************************/
    @Headers("host:$SERVER_HOST_B")
    @GET("user/loginWithScanCode")
    fun bMethod1(@Query("id") id: Int): Observable<ResponseBody>
}
```

### 方法2：多套服务类，实例化为多个对象，准确查找接口归属服务
定义多个类，每个类定义一套服务接口。然后分别实例化为多个对象，再使用准确的对象来调用接口。这种方法运行效率是最高的，但是在开发时，可能无法快速知道接口归属与哪个服务，需要查看代码才能准确知晓，可以说是少了代码提示能力。

```
interface ApiA {
    @GET("user/loginWithScanCode")
    fun methodA(@Query("id") id: Int): Observable<ResponseBody>
}

interface ApiB {
    @GET("user/loginWithScanCode")
    fun methodB(@Query("id") id: Int): Observable<ResponseBody>
}
```

### 方法3：全写在一起，实例化为多个对象，准确调用方法
把所有接口都写在一个类中，然后根据服务地址分别实例化为多个对象。再准确调用方法，为了保证准确调用方法，可以给每个接口加个服务名的前缀，以减少方法调错的问题。


```
interface ApiAllInOne {
    /************************** server A ****************************/
    @GET("user/loginWithScanCode")
    fun aMethod1(@Query("id") id: Int): Observable<ResponseBody>

    /************************** server B ****************************/
    @GET("user/loginWithScanCode")
    fun bMethod1(@Query("id") id: Int): Observable<ResponseBody>
}

const val SERVER_HOST_A = "https://www.a.com/"
const val SERVER_HOST_B = "https://www.b.com/"
fun getApi(retrofit: Retrofit, host: String): ApiAllInOne {
    return retrofit.newBuilder()
            .baseUrl(host).build()
            .create(ApiAllInOne::class.java)
}

fun showNomalUseCase(retrofit: Retrofit) {
    val apiA = getApi(retrofit, SERVER_HOST_A)//save as single instance for repeated usage
    apiA.aMethod1(1).subscribe()
    apiA.bMethod1(1).subscribe()//invalid usage, but no compile error

    val apiB = getApi(retrofit, SERVER_HOST_B)
    apiB.bMethod1(1).subscribe()
    apiB.aMethod1(1).subscribe()//invalid usage, but no compile error
}
```
# 有更简单的方法吗？
当然有了，而且超方便！

## 定义接口
（建议）在一个KT文件中定义所有接口，方便查找和维护。
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
## 建工具类
一般都需要个工具类的，方便配置拦截器等。如果没有自定义的需求，也可以直接实例化来用。

可以重写invokeApi方法，全局给每个Observable设定线程。

```
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
```
## 动态更新服务地址
还可以动态更新服务地址，比如实现测试服务和正式服务间切换。
```
    //update api baseUrl when needed
    apiUtil.updateApi(ApiA::class, "https://www.a2.com/")
```

## 调用接口
```
    api.methodA(1).subscribe()
    api.methodB(1).subscribe()
```
# 引入依赖
```
dependencies {
    implementation 'com.github.DonaldDu:ApiHolder:x.x.x'//JitPack version
}
```
## 该项目使用的三方库
- OkHttp3
- Retrofit2
- rxjava3（可以修改为rxjava2）

```
    api 'com.squareup.okhttp3:okhttp:4.7.2'
    api "com.squareup.retrofit2:retrofit:2.9.0"
    api "com.squareup.retrofit2:converter-gson:2.9.0"
    api "com.squareup.retrofit2:adapter-rxjava3:2.9.0"
    api 'io.reactivex.rxjava3:rxandroid:3.0.0'
```
# 其它说明
## rxjava3 ->rxjava2
可以根据需要调整为rxjava2，建议用最新的。

```
    //重写ApiHolderUtil如下方法，RxJava3CallAdapterFactory ->RxJava2CallAdapterFactory即可。
    protected open fun getRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
                .validateEagerly(validateEagerly)
                .addConverterFactory(getGsonConverterFactory())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .baseUrl("http://www.demo.com/")
                .client(client)
                .build()
    }
```
## Timeout
可以给每套服务设置不同的超时
```
@BaseUrl("https://www.b.com/")
@Timeout(read = 100, timeUnit = TimeUnit.SECONDS)
interface ApiB {
    @GET("user/loginWithScanCode")
    fun methodB(@Query("id") id: Int): Observable<ResponseBody>
}
```
