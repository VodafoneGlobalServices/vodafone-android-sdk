package com.vodafone.global.sdk.http.oauth;

import com.squareup.okhttp.*;
import com.vodafone.global.sdk.http.ResponseHolder;
import com.vodafone.global.sdk.logging.LogUtil;
import com.vodafone.global.sdk.logging.Logger;

/**
 * OAuth 2 request processor. Builds HTTP request and handles response.
 */
public class OAuthTokenRequest extends com.vodafone.global.sdk.http.Request {

    private final String url;
    private final String clientId;
    private final String clientSecret;
    private final String scope;
    private final String grantType;
    private final Logger logger;


    /**
     * Provides builder for {@link OAuthTokenRequest}.
     */
    public static Builder builder() {
        return new Builder();
    }

    protected OAuthTokenRequest(String url, String clientId, String clientSecret, String scope, String grantType, Logger logger) {
        this.url = url;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.scope = scope;
        this.grantType = grantType;
        this.logger = logger;
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

        logger.d(LogUtil.prepareRequestLogMsg(request));

        OkHttpClient client = getOkHttpClient();
        Response response = client.newCall(request).execute();
        ResponseHolder responseHolder = new ResponseHolder(response);

        logger.d(LogUtil.prepareResponseLogMsg(responseHolder));
        return responseHolder;
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
        private Logger logger;

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

        public Builder logger(Logger logger) {
            this.logger = logger;
            return this;
        }

        public OAuthTokenRequest build() {
            return new OAuthTokenRequest(url, clientId, clientSecret, scope, grantType, logger);
        }
    }
}
