package com.vodafone.global.sdk.http.sms;

import com.octo.android.robospice.request.okhttp.OkHttpSpiceRequest;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.util.UUID;

public class PinRequest extends OkHttpSpiceRequest<Void> {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final String url;
    private final String accessToken;
    private final String androidId;
    private final String mobileCountryCode;
    private final String sdkId;
    private final String appId;

    /**
     * Provides builder for {@link PinRequest}.
     */
    public static Builder builder() {
        return new Builder();
    }

    protected PinRequest(
            String url, String accessToken, String androidId, String mobileCountryCode,
            String sdkId, String appId
    ) {
        super(Void.class);
        this.url = url;
        this.accessToken = accessToken;
        this.androidId = androidId;
        this.mobileCountryCode = mobileCountryCode;
        this.sdkId = sdkId;
        this.appId = appId;
    }

    @Override
    public Void loadDataFromNetwork() throws Exception {
        // e.g. POST https://APIX/he/users/tokens/sendPIN/{token}
        RequestBody emptyBody = RequestBody.create(JSON, "");
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("User-Agent", sdkId)
                .addHeader("Application-ID", appId)
                .addHeader("x-vf-trace-subject-id", androidId)
                .addHeader("x-vf-trace-subject-region", mobileCountryCode)
                .addHeader("x-vf-trace-source", sdkId + "" + appId)
                .addHeader("x-vf-trace-transaction-id", UUID.randomUUID().toString())
                .post(emptyBody)
                .build();
        OkHttpClient client = getOkHttpClient();
        Response response = client.newCall(request).execute();
        int code = response.code();
        switch (code) {
            case 201:
                // we are only interested in HTTP code, body is empty anyway so we return null
                return null;
            default:
                throw new IllegalStateException(); // TODO better exception
        }
    }

    /**
     * Builder for {@link PinRequest}.
     * @see PinRequest#builder()
     */
    public static class Builder {

        private String url;
        private String accessToken;
        private String androidId;
        private String mobileCountryCode;
        private String sdkId;
        private String appId;

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

        public PinRequest build() {
            return new PinRequest(url, accessToken, androidId, mobileCountryCode, sdkId, appId);
        }
    }
}
