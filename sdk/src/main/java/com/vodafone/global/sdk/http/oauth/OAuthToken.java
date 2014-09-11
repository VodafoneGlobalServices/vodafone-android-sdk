package com.vodafone.global.sdk.http.oauth;

public class OAuthToken {
    public final String accessToken;
    public final String tokenType;
    public final String expiresIn;
    public final long expirationTime;

    public OAuthToken(String accessToken, String tokenType, String expiresIn, long expirationTime) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.expirationTime = expirationTime;
    }
}
