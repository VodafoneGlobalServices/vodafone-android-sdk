package com.vodafone.global.sdk.http.oauth;

import android.net.Uri;
import com.squareup.okhttp.OkHttpClient;
import com.vodafone.global.sdk.Settings;
import com.vodafone.global.sdk.http.ResponseHolder;
import com.vodafone.global.sdk.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import static com.vodafone.global.sdk.http.HttpCode.OK_200;

public class OAuthProcessor {
    private final OkHttpClient httpClient;
    private final String clientAppKey;
    private final String clientAppSecret;
    private final Settings settings;
    private final Logger logger;

    public OAuthProcessor(OkHttpClient httpClient, String clientAppKey, String clientAppSecret, Settings settings, Logger logger) {
        this.httpClient = httpClient;
        this.clientAppKey = clientAppKey;
        this.clientAppSecret = clientAppSecret;
        this.settings = settings;
        this.logger = logger;
    }

    public OAuthToken process() throws Exception {
        String uri = new Uri.Builder()
                .scheme(settings.oauth().protocol())
                .authority(settings.oauth().host())
                .path(settings.oauth().path())
                .build()
                .toString();
        OAuthTokenRequest request = OAuthTokenRequest.builder()
                .url(uri)
                .clientId(clientAppKey)
                .clientSecret(clientAppSecret)
                .scope(settings.oAuthTokenScope())
                .grantType(settings.oAuthTokenGrantType())
                .logger(logger)
                .build();

        request.setOkHttpClient(httpClient);
        ResponseHolder responseHolder = request.loadDataFromNetwork();
        int code = responseHolder.code();
        switch (code) {
            case OK_200:
                return parseJson(responseHolder);
            default:
                throw new AuthorizationFailed();
        }
    }

    private OAuthToken parseJson(ResponseHolder responseHolder) throws JSONException, IOException {
        JSONObject json = new JSONObject(responseHolder.body());
        String accessToken = json.getString("access_token");
        String tokenType = json.getString("token_type");
        String expiresIn = json.getString("expires_in");
        long expirationTime = System.currentTimeMillis() + Integer.getInteger(expiresIn, 0);
        return new OAuthToken(accessToken, tokenType, expiresIn, expirationTime);
    }
}
