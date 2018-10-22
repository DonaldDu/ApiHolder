package com.dhy.apiholder;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.SingleTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiHolderUtil {
    private Retrofit retrofit;
    private boolean isAndroidApi;
    private final Map<Class, Object> apis = new HashMap<>();
    private final Map<Class, String> baseUrls = new HashMap<>();
    private AutoCmdUtil autoCmdUtil;

    protected boolean isAndroidApi() {
        return true;
    }

    protected boolean validateEagerly() {
        return false;
    }

    protected OkHttpClient getClient() {
        return new OkHttpClient();
    }

    protected Retrofit getRetrofit(@NonNull OkHttpClient client) {
        return new Retrofit.Builder()
                .validateEagerly(validateEagerly())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl("http://www.demo.com/")
                .client(client)
                .build();
    }

    private void init() {
        this.isAndroidApi = isAndroidApi();
        this.retrofit = getRetrofit(getClient());
        try {
            autoCmdUtil = new AutoCmdUtil();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final <HOLDER> HOLDER createHolderApi(@NonNull Class<HOLDER> apiHolder) {
        init();
        Class[] partApis = apiHolder.getInterfaces();
        for (Class api : partApis) {
            updateApi(api);
        }
        return createHolderApi(apis, apiHolder);
    }

    /**
     * update api with @{@link BaseUrl}
     */
    public final <API> void updateApi(@NonNull Class<API> api) {
        updateApi(api, null);
    }

    public final <API> void updateApi(@NonNull Class<API> apiClass, @Nullable String baseUrl) {
        Retrofit.Builder retrofitBuilder = retrofit.newBuilder();
        initTimeout(retrofitBuilder, apiClass);
        baseUrl = getBaseUrl(apiClass, baseUrl);
        Retrofit retrofit = retrofitBuilder.baseUrl(baseUrl).build();
        apis.put(apiClass, retrofit.create(apiClass));
        baseUrls.put(apiClass, baseUrl);

        if (validateEagerly() && autoCmdUtil != null) {
            try {
                autoCmdUtil.initHeaders(retrofit);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private <API> void initTimeout(Retrofit.Builder retrofitBuilder, @NonNull Class<API> apiClass) {
        if (apiClass.isAnnotationPresent(Timeout.class)) {
            Timeout timeout = apiClass.getAnnotation(Timeout.class);
            OkHttpClient.Builder builder = getClient().newBuilder();
            if (timeout.connect() > 0) {
                builder.connectTimeout(timeout.connect(), timeout.timeUnit());
            }
            if (timeout.read() > 0) {
                builder.readTimeout(timeout.read(), timeout.timeUnit());
            }
            if (timeout.write() > 0) {
                builder.writeTimeout(timeout.write(), timeout.timeUnit());
            }
            retrofitBuilder.client(builder.build());
        }
    }

    @NonNull
    public <API> String getCurrentBaseUrl(@NonNull Class<API> api) {
        return baseUrls.get(api);
    }

    /**
     * get full url with append
     */
    public static <API> String getBaseUrl(@NonNull Class<API> api, @Nullable String newBaseUrl) {
        if (api.isAnnotationPresent(BaseUrl.class)) {
            BaseUrl baseUrl = api.getAnnotation(BaseUrl.class);
            String url = newBaseUrl != null ? newBaseUrl : baseUrl.value();
            if (!url.endsWith("/")) url += "/";

            String append = baseUrl.append();
            if (!TextUtils.isEmpty(append)) {
                if (append.startsWith("/")) append = append.substring(1);
                url = url + append;
            }
            if (!url.endsWith("/")) url += "/";
            return url;
        } else {
            throw new IllegalArgumentException(String.format("%s: MUST ANNOTATE WITH '%s'", api.getName(), BaseUrl.class.getName()));
        }
    }

    /**
     * eg: BaseUrl(value="https://www.a.com/", append = "apiA") => "https://www.a.com"
     *
     * @return current url without append & '/'
     */
    public <API> String getCurrentUrlWithoutAppend(@NonNull Class<API> api) {
        if (api.isAnnotationPresent(BaseUrl.class)) {
            BaseUrl baseUrl = api.getAnnotation(BaseUrl.class);
            String append = baseUrl.append();
            String url = getCurrentBaseUrl(api);
            if (url.endsWith("/")) url = url.substring(0, url.length() - 1);
            if (TextUtils.isEmpty(append)) {
                return url;
            } else {
                if (append.endsWith("/")) append = append.substring(0, append.length() - 1);
                url = url.substring(0, url.length() - append.length());
                if (url.endsWith("/")) url = url.substring(0, url.length() - 1);
                return url;
            }
        } else {
            throw new IllegalArgumentException(String.format("%s: MUST ANNOTATE WITH '%s'", api.getName(), BaseUrl.class.getName()));
        }
    }

    /**
     * get url from with @{@link BaseUrl}  with append
     */
    @NonNull
    public static <API> String getBaseUrl(@NonNull Class<API> api) {
        return getBaseUrl(api, null);
    }

    /**
     * hold all api in one
     */
    private <HOLDER> HOLDER createHolderApi(@NonNull final Map<Class, Object> apis, @NonNull Class<HOLDER> apiHolder) {
        return (HOLDER) Proxy.newProxyInstance(apiHolder.getClassLoader(), new Class[]{apiHolder}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Object api = apis.get(method.getDeclaringClass());
                return invokeApi(api, method, args, isAndroidApi);
            }
        });
    }

    protected Object invokeApi(@NonNull Object api, @NonNull Method method, Object[] args, boolean isAndroidApi) throws Throwable {
        Object result = method.invoke(api, args);
        return isAndroidApi ? setAndroidSchedulers(result) : result;
    }

    protected Object setAndroidSchedulers(@NonNull Object result) {
        if (result instanceof Single) {
            return ((Single) result).compose(new SingleTransformer() {
                @Override
                public SingleSource apply(Single upstream) {
                    return upstream.subscribeOn(Schedulers.io())
                            .unsubscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread());
                }
            });
        } else if (result instanceof Observable) {
            return ((Observable) result).compose(new ObservableTransformer() {
                @Override
                public ObservableSource apply(Observable upstream) {
                    return upstream.subscribeOn(Schedulers.io())
                            .unsubscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread());
                }
            });
        }
        return result;
    }
}
