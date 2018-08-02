package com.dhy.apiholder;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
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
    @NonNull
    protected OkHttpClient client;
    @NonNull
    protected Retrofit retrofit;
    @NonNull
    protected final Map<Class, Object> holder = new HashMap<>();

    public ApiHolderUtil(@Nullable OkHttpClient client, @Nullable Retrofit retrofit) {
        this.client = client != null ? client : new OkHttpClient();
        this.retrofit = retrofit != null ? retrofit : getRetrofit(this.client);
    }

    protected Retrofit getRetrofit(@NonNull OkHttpClient client) {
        return new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl("http://www.demo.com/")
                .client(client)
                .build();
    }

    public <API_HOLDER> API_HOLDER createHolderApi(@NonNull Class<API_HOLDER> apiHolder, @NonNull List<PartApi> partApis, final boolean android) {
        for (PartApi part : partApis) {
            updatePartApi(part.baseUrl, part.api);
        }
        return createHolderApi(apiHolder, android, holder);
    }

    public <API> void updatePartApi(String baseUrl, Class<API> apiClass) {
        Retrofit retrofit = this.retrofit.newBuilder().baseUrl(baseUrl).build();
        holder.put(apiClass, retrofit.create(apiClass));
    }

    /**
     * hold all api in one
     */
    protected <API_HOLDER> API_HOLDER createHolderApi(Class<API_HOLDER> apiHolder, final boolean android, final Map<Class, Object> holder) {
        return (API_HOLDER) Proxy.newProxyInstance(apiHolder.getClassLoader(), new Class<?>[]{apiHolder}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Object api = holder.get(method.getDeclaringClass());
                Object result = method.invoke(api, args);
                if (android) return setAndroidSchedulers(result);
                return result;
            }
        });
    }

    protected Object setAndroidSchedulers(Object result) {
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
