package com.vodafone.global.sdk;

import com.squareup.okhttp.Request;

import java.util.UUID;

public class RequestBuilderProvider {
    private String sdkId;
    private String androidId;
    private String mobileCountryCode;
    private String backendAppKey;

    public RequestBuilderProvider(String sdkId, String androidId, String mobileCountryCode, String backendAppKey) {
        this.sdkId = sdkId;
        this.androidId = androidId;
        this.mobileCountryCode = mobileCountryCode;
        this.backendAppKey = backendAppKey;
    }

    public Request.Builder builder() {
        return new Request.Builder()
                .addHeader("Accept", "application/json")
                .addHeader("User-Agent", sdkId)
                .addHeader("x-vf-trace-subject-id", androidId)
                .addHeader("x-vf-trace-subject-region", mobileCountryCode)
                .addHeader("x-vf-trace-source", sdkId + "" + backendAppKey)
                .addHeader("x-vf-trace-transaction-id", UUID.randomUUID().toString());
    }
}
