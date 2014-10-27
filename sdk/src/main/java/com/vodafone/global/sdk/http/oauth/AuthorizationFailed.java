package com.vodafone.global.sdk.http.oauth;

import com.vodafone.global.sdk.VodafoneException;

public class AuthorizationFailed extends VodafoneException {
    public AuthorizationFailed() {
        super(Type.AUTHORIZATION_FAILED);
    }
}
