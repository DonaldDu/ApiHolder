package com.dhy.apiholder.demo;

import android.database.Observable;
import android.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.dhy.apiholder.ApiHolderUtil;
import com.dhy.apiholder.PartApi;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initApiHoder();
    }

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
}
