package com.vodafone.global.sdk.http;

import com.squareup.okhttp.OkHttpClient;

public abstract class Request {
    private OkHttpClient okHttpClient;

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public void setOkHttpClient(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
    }

    public abstract ResponseHolder loadDataFromNetwork() throws Exception;
}
