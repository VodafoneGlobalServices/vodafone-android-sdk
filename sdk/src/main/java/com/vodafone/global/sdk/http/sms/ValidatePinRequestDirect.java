package com.vodafone.global.sdk.http.sms;


import com.octo.android.robospice.request.okhttp.OkHttpSpiceRequest;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.UUID;

public class ValidatePinRequestDirect extends OkHttpSpiceRequest<Response> {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final String url;
    private final String accessToken;
    private final String androidId;
    private final String mobileCountryCode;
    private final String sdkId;
    private final String appId;
    private final String pin;

    /**
     * Provides builder for {@link com.vodafone.global.sdk.http.sms.ValidatePinRequestDirect}.
     */
    public static Builder builder() {
        return new Builder();
    }

    protected ValidatePinRequestDirect(
            String url, String accessToken, String androidId, String mobileCountryCode,
            String sdkId, String appId, String pin
    ) {
        super(Response.class);
        this.url = url;
        this.accessToken = accessToken;
        this.androidId = androidId;
        this.mobileCountryCode = mobileCountryCode;
        this.sdkId = sdkId;
        this.appId = appId;
        this.pin = pin;
    }

    @Override
    public Response loadDataFromNetwork() throws IOException, JSONException {
        RequestBody body = RequestBody.create(JSON, prepareBody(pin));
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("User-Agent", sdkId)
                .addHeader("x-vf-trace-subject-id", androidId)
                .addHeader("x-vf-trace-subject-region", mobileCountryCode)
                .addHeader("x-vf-trace-source", sdkId + "" + appId)
                .addHeader("x-vf-trace-transaction-id", UUID.randomUUID().toString())
                .post(body)
                .build();
        OkHttpClient client = getOkHttpClient();
        return client.newCall(request).execute();
    }

    protected String prepareBody(String pin) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("code", pin);
        return json.toString();
    }

    public static class Builder {

        private String url;
        private String accessToken;
        private String androidId;
        private String mobileCountryCode;
        private String sdkId;
        private String appId;
        private String pin;

        private Builder() {
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public Builder androidId(String androidId) {
            this.androidId = androidId;
            return this;
        }

        public Builder mobileCountryCode(String mobileCountryCode) {
            this.mobileCountryCode = mobileCountryCode;
            return this;
        }

        public Builder sdkId(String sdkId) {
            this.sdkId = sdkId;
            return this;
        }

        public Builder appId(String appId) {
            this.appId = appId;
            return this;
        }

        public Builder pin(String pin) {
            this.pin = pin;
            return this;
        }

        public ValidatePinRequestDirect build() {
            return new ValidatePinRequestDirect(url, accessToken, androidId, mobileCountryCode, sdkId, appId, pin);
        }
    }
}
