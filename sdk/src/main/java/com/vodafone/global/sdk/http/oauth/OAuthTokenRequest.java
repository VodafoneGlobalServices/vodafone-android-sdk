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

import static com.vodafone.global.sdk.http.HttpCode.OK_200;

/**
 * OAuth 2 request processor. Builds HTTP request and handles response.
 */
public class OAuthTokenRequest extends OkHttpSpiceRequest<OAuthToken> {

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
        super(OAuthToken.class);
        this.url = url;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.scope = scope;
        this.grantType = grantType;
    }

    @Override
    public OAuthToken loadDataFromNetwork() throws Exception {
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
        int code = response.code();
        switch (code) {
            case OK_200:
                return parseJson(response);
            default:
                throw new AuthorizationFailed();
        }
    }

    private OAuthToken parseJson(Response response) throws JSONException, IOException {
        JSONObject json = new JSONObject(response.body().string());
        String accessToken = json.getString("access_token");
        String tokenType = json.getString("token_type");
        String expiresIn = json.getString("expires_in");
        long expirationTime = System.currentTimeMillis() + Integer.getInteger(expiresIn, 0);
        return new OAuthToken(accessToken, tokenType, expiresIn, expirationTime);
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
