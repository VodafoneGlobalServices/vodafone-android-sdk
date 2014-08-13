package com.vodafone.global.sdk.http;

import com.octo.android.robospice.request.okhttp.OkHttpSpiceRequest;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * OAuth 2 request processor. Builds HTTP request and handles response.
 */
public class OAuthTokenRequest extends OkHttpSpiceRequest<OAuthToken> {

    private final String url;
    private final String clientId;
    private final String clientSecret;

    /**
     * Provides builder for {@link com.vodafone.global.sdk.http.OAuthTokenRequest}.
     */
    public static Builder builder() {
        return new Builder();
    }

    protected OAuthTokenRequest(String url, String clientId, String clientSecret) {
        super(OAuthToken.class);
        this.url = url;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
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

    public static class Builder {
        private String url;
        private String clientId;
        private String clientSecret;

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

        public OAuthTokenRequest build() {
            return new OAuthTokenRequest(url, clientId, clientSecret);
        }
    }

}
