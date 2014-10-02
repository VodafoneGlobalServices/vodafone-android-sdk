package com.vodafone.global.sdk;

import android.os.Build;
import com.squareup.okhttp.Request;

import java.util.UUID;

public class RequestBuilderProvider {
    private String sdkId;
    private String androidId;
    private String mobileCountryCode;
    private String backendAppKey;
    private final String clientAppKey;

    public RequestBuilderProvider(String sdkId, String androidId, String mobileCountryCode, String backendAppKey, String clientAppKey) {
        this.sdkId = sdkId;
        this.androidId = androidId;
        this.mobileCountryCode = mobileCountryCode;
        this.backendAppKey = backendAppKey;
        this.clientAppKey = clientAppKey;
    }

    public Request.Builder builder() {
        return new Request.Builder()
                .addHeader("Accept", "application/json")
                .addHeader("User-Agent", sdkId)
                .addHeader("x-vf-trace-subject-id", Build.MANUFACTURER + " " + Build.MODEL + " \\ "
                        + Build.VERSION.RELEASE + " \\ " + androidId)
                .addHeader("x-vf-trace-subject-region", mobileCountryCode)
                .addHeader("x-vf-trace-source", sdkId + "-" + clientAppKey + "-" + backendAppKey)
                .addHeader("x-vf-trace-transaction-id", UUID.randomUUID().toString());
    }
}
