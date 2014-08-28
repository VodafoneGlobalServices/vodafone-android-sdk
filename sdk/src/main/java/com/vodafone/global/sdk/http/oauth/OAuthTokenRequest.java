package com.vodafone.global.sdk.http.oauth;

import com.octo.android.robospice.request.okhttp.OkHttpSpiceRequest;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.UUID;

/**
 * OAuth 2 request processor. Builds HTTP request and handles response.
 */
public class OAuthTokenRequest extends OkHttpSpiceRequest<OAuthToken> {

    private final String url;
    private final String clientId;
    private final String clientSecret;
    private final String androidId;
    private final String mobileCountryCode;
    private final String sdkId;
    private final String appId;

    /**
     * Provides builder for {@link OAuthTokenRequest}.
     */
    public static Builder builder() {
        return new Builder();
    }

    protected OAuthTokenRequest(
            String url, String clientId, String clientSecret, String androidId,
            String mobileCountryCode, String sdkId, String appId
    ) {
        super(OAuthToken.class);
        this.url = url;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.androidId = androidId;
        this.mobileCountryCode = mobileCountryCode;
        this.sdkId = sdkId;
        this.appId = appId;
    }

    @Override
    public OAuthToken loadDataFromNetwork() throws Exception {
        RequestBody body = new FormEncodingBuilder()
                .add("grant_type", "client_credentials") // TODO might need to change, depends on 3rd party
                .add("client_id", clientId)
                .add("client_secret", clientSecret)
                .add("scope", "SSO_OAUTH2_INPUT") // TODO might need to change, depends on 3rd party
                .build();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("x-vf-trace-subject-id", androidId)
                .addHeader("x-vf-trace-subject-region", mobileCountryCode)
                .addHeader("x-vf-trace-source", sdkId + "" + appId)
                .addHeader("x-vf-trace-transaction-id", UUID.randomUUID().toString())
                .post(body)
                .build();
        OkHttpClient client = getOkHttpClient();
        Response response = client.newCall(request).execute();
        int code = response.code();
        switch (code) {
            case 200:
                return parseJson(response);
            default:
                throw new IllegalStateException(); // TODO better exception
        }
    }

    private OAuthToken parseJson(Response response) throws JSONException, IOException {
        JSONObject json = new JSONObject(response.body().string());
        String accessToken = json.getString("access_token");
        String tokenType = json.getString("token_type");
        String expiresIn = json.getString("expires_in");
        return new OAuthToken(accessToken, tokenType, expiresIn);
    }

    /**
     * Builder for {@link OAuthTokenRequest}.
     * @see OAuthTokenRequest#builder()
     */
    public static class Builder {

        private String url;
        private String clientId;
        private String clientSecret;
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

        public Builder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder clientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
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

        public OAuthTokenRequest build() {
            return new OAuthTokenRequest(url, clientId, clientSecret, androidId, mobileCountryCode, sdkId, appId);
        }
    }
}
