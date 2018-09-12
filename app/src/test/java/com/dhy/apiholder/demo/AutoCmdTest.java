package com.dhy.apiholder.demo;

import android.support.annotation.NonNull;

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import io.reactivex.Observable;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public class AutoCmdTest {
    @Test
    public void testMe() throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        Retrofit retrofit = getRetrofit(getClient());
        API api = retrofit.create(API.class);
        initHeaders(retrofit);
        api.logout();
    }

    private void initHeaders(Retrofit retrofit) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        Map<Method, Object> serviceMethodCache = getServiceMethodCache(retrofit);
        Set<Method> methods = serviceMethodCache.keySet();
        for (Method method : methods) {
            Object logout = serviceMethodCache.get(method);
            getHeaders(logout);
        }
    }

    private void getHeaders(Object serviceMethodInstance) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        Class<?> ServiceMethod = Class.forName("retrofit2.ServiceMethod");
        Field headersF = ServiceMethod.getDeclaredField("headers");
        headersF.setAccessible(true);
        Headers headers = (Headers) headersF.get(serviceMethodInstance);
        Headers.Builder builder;
        if (headers == null) {
            builder = new Headers.Builder();
        } else {
            builder = headers.newBuilder();
        }
        headers = builder.add("cmd", "api/v1/sys/sysUserInfo/logout").build();
        headersF.set(serviceMethodInstance, headers);
    }

    private Map<Method, Object> getServiceMethodCache(Retrofit retrofit) throws NoSuchFieldException, IllegalAccessException {
        //serviceMethodCache
        Field serviceMethodCache = Retrofit.class.getDeclaredField("serviceMethodCache");
        serviceMethodCache.setAccessible(true);
        return (Map<Method, Object>) serviceMethodCache.get(retrofit);
    }

    private OkHttpClient getClient() {
        return new OkHttpClient();
    }

    private Retrofit getRetrofit(@NonNull OkHttpClient client) {
        return new Retrofit.Builder()
                .validateEagerly(true)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl("http://www.demo.com/")
                .client(client)
                .build();
    }

    interface API {
        @GET("api/v1/sys/sysUserInfo/logout")
        Observable<ResponseBody> logout();
    }
}
