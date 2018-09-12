package com.dhy.apiholder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import okhttp3.Headers;
import retrofit2.Retrofit;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public class AutoCmdUtil {
    private Field serviceMethodCacheF;
    private Field headersF;
    @SuppressWarnings("FieldCanBeLocal")
    private final String HEADER_CMD = "cmd";

    public AutoCmdUtil() throws Exception {
        init();
    }

    private void init() throws NoSuchFieldException, ClassNotFoundException {
        serviceMethodCacheF = Retrofit.class.getDeclaredField("serviceMethodCache");
        serviceMethodCacheF.setAccessible(true);

        Class<?> ServiceMethod = Class.forName("retrofit2.ServiceMethod");
        headersF = ServiceMethod.getDeclaredField("headers");
        headersF.setAccessible(true);
    }

    public void initHeaders(Retrofit retrofit) throws IllegalAccessException {
        Map<Method, Object> serviceMethodCache = getServiceMethodCache(retrofit);
        Set<Method> methods = serviceMethodCache.keySet();
        for (Method method : methods) {
            Object serviceMethodInstance = serviceMethodCache.get(method);
            initHeaders(method, serviceMethodInstance);
        }
    }

    private void initHeaders(Method method, Object serviceMethodInstance) throws IllegalAccessException {
        Headers headers = (Headers) headersF.get(serviceMethodInstance);
        Headers.Builder builder;
        if (headers == null) {
            builder = new Headers.Builder();
        } else {
            builder = headers.newBuilder();
        }
        if (builder.get(HEADER_CMD) == null) {
            headers = builder.add(HEADER_CMD, getCmd(method)).build();
        }
        headersF.set(serviceMethodInstance, headers);
    }

    private Map<Method, Object> getServiceMethodCache(Retrofit retrofit) throws IllegalAccessException {
        return (Map<Method, Object>) serviceMethodCacheF.get(retrofit);
    }

    protected String getCmd(Method method) {
        GET get = method.getAnnotation(GET.class);
        if (get != null) return get.value();

        POST post = method.getAnnotation(POST.class);
        if (post != null) return post.value();

        PUT put = method.getAnnotation(PUT.class);
        if (put != null) return put.value();

        DELETE delete = method.getAnnotation(DELETE.class);
        if (delete != null) return delete.value();

        PATCH patch = method.getAnnotation(PATCH.class);
        if (patch != null) return patch.value();

        throw new IllegalArgumentException("unsupport http Method for cmd");
    }
}
