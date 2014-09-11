package com.vodafone.global.sdk.http.resolve;

import android.util.Log;

import com.octo.android.robospice.request.okhttp.OkHttpSpiceRequest;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.vodafone.global.sdk.SimSerialNumber;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.UUID;

public class ResolvePostRequestDirect extends OkHttpSpiceRequest<Response> {
    private static final String TAG = ResolvePostRequestDirect.class.getSimpleName();
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final String url;
    private final String accessToken;
    private final String androidId;
    private final String mobileCountryCode;
    private final String sdkId;
    private final String appId;
    private final SimSerialNumber imsi;
    private final boolean smsValidation;

    /**
     * Provides builder for {@link com.vodafone.global.sdk.http.resolve.ResolvePostRequestDirect}.
     */
    public static Builder builder() {
        return new Builder();
    }

    protected ResolvePostRequestDirect(
            String url, String accessToken, String androidId, String mobileCountryCode,
            String sdkId, String appId, SimSerialNumber imsi, boolean smsValidation
    ) {
        super(Response.class);
        this.url = url;
        this.accessToken = accessToken;
        this.androidId = androidId;
        this.mobileCountryCode = mobileCountryCode;
        this.sdkId = sdkId;
        this.appId = appId;
        this.imsi = imsi;
        this.smsValidation = smsValidation;
    }

    @Override
    public Response loadDataFromNetwork() throws IOException, JSONException {
        RequestBody body = RequestBody.create(JSON, prepareBody(imsi, smsValidation));
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("User-Agent", sdkId)
                .addHeader("scope", "seamless_id_user_details_all") //TODO: REMOVE ONLY FOR TESTING!!!
                .addHeader("x-int-opco-id", "DE") //TODO: REMOVE ONLY FOR TESTING!!!
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("x-vf-trace-subject-id", androidId)
                .addHeader("x-vf-trace-subject-region", mobileCountryCode)
                .addHeader("x-vf-trace-source", sdkId + "-" + appId)
                .addHeader("x-vf-trace-transaction-id", UUID.randomUUID().toString())
                .post(body)
                .build();
        OkHttpClient client = getOkHttpClient();
        return client.newCall(request).execute();
    }

    protected String prepareBody(SimSerialNumber imsi, boolean smsValidation) throws JSONException {
        JSONObject json = new JSONObject();
        if (imsi.isPresent()) {
            //json.put("imsi", imsi.get() /*"204049810027400"*/);
            json.put("imsi", "204049810027400");
        }
        json.put("smsValidation", smsValidation);
        return json.toString();
    }

    /**
     * Builder for {@link com.vodafone.global.sdk.http.resolve.ResolvePostRequestDirect}.
     * @see com.vodafone.global.sdk.http.resolve.ResolvePostRequestDirect#builder()
     */
    public static class Builder {

        private String url;
        private String accessToken;
        private String androidId;
        private String mobileCountryCode;
        private String sdkId;
        private String appId;
        private SimSerialNumber imsi;
        private boolean smsValidation;

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

        public Builder imsi(SimSerialNumber imsi) {
            this.imsi = imsi;
            return this;
        }

        public Builder smsValidation(boolean smsValidation) {
            this.smsValidation = smsValidation;
            return this;
        }

        public ResolvePostRequestDirect build() {
            return new ResolvePostRequestDirect(url, accessToken, androidId, mobileCountryCode, sdkId, appId, imsi, smsValidation);
        }
    }
}
