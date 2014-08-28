package com.vodafone.global.sdk.http.oauth;

public class OAuthToken {
    public final String accessToken;
    public final String tokenType;
    public final String expiresIn;

    public OAuthToken(String accessToken, String tokenType, String expiresIn) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
    }
}
