package com.dhy.apiholder;

public class PartApi {
    public final Class api;
    public final String baseUrl;

    public PartApi(Class api, String baseUrl) {
        this.api = api;
        this.baseUrl = baseUrl;
    }
}
