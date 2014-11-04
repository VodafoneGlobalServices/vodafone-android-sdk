package com.vodafone.global.sdk.http.oauth;

import com.squareup.okhttp.*;
import com.vodafone.global.sdk.http.ResponseHolder;

/**
 * OAuth 2 request processor. Builds HTTP request and handles response.
 */
public class OAuthTokenRequest extends com.vodafone.global.sdk.http.Request {

    private final String url;
    private final String clientId;
    private final String clientSecret;
    private final String scope;
    private final String grantType;


    /**
     * Provides builder for {@link OAuthTokenRequest}.
     */
    public static Builder builder() {
        return new Builder();
    }

    protected OAuthTokenRequest(String url, String clientId, String clientSecret, String scope, String grantType) {
        this.url = url;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.scope = scope;
        this.grantType = grantType;
    }

    @Override
    public ResponseHolder loadDataFromNetwork() throws Exception {
        RequestBody body = new FormEncodingBuilder()
                .add("grant_type", grantType)
                .add("client_id", clientId)
                .add("client_secret", clientSecret)
                .add("scope", scope)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("Content-type", "application/x-www-form-urlencoded")
                .post(body)
                .build();

        OkHttpClient client = getOkHttpClient();
        Response response = client.newCall(request).execute();
        return new ResponseHolder(response);
    }

    /**
     * Builder for {@link OAuthTokenRequest}.
     * @see OAuthTokenRequest#builder()
     */
    public static class Builder {

        private String url;
        private String clientId;
        private String clientSecret;
        private String scope;
        private String grantType;

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

        public Builder scope(String scope) {
            this.scope = scope;
            return this;
        }

        public Builder grantType(String grantType) {
            this.grantType = grantType;
            return this;
        }

        public OAuthTokenRequest build() {
            return new OAuthTokenRequest(url, clientId, clientSecret, scope, grantType);
        }
    }
}
