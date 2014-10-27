package com.vodafone.global.sdk.http.oauth;

import android.net.Uri;
import com.squareup.okhttp.OkHttpClient;
import com.vodafone.global.sdk.Settings;

public class OAuthProcessor {
    private final String clientAppKey;
    private final String clientAppSecret;
    private final Settings settings;

    public OAuthProcessor(String clientAppKey, String clientAppSecret, Settings settings) {
        this.clientAppKey = clientAppKey;
        this.clientAppSecret = clientAppSecret;
        this.settings = settings;
    }

    public OAuthToken process() throws Exception {
        Uri.Builder builder = new Uri.Builder();
        Uri uri = builder.scheme(settings.oauth.protocol).authority(settings.oauth.host).path(settings.oauth.path).build();
        OAuthTokenRequest request = OAuthTokenRequest.builder()
                .url(uri.toString())
                .clientId(clientAppKey)
                .clientSecret(clientAppSecret)
                .scope(settings.oAuthTokenScope)
                .grantType(settings.oAuthTokenGrantType)
                .build();

        request.setRetryPolicy(null);
        request.setOkHttpClient(new OkHttpClient());
        return request.loadDataFromNetwork();
    }
}
