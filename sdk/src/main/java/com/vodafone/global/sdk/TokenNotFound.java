package com.vodafone.global.sdk;

public class TokenNotFound extends VodafoneException {
    public TokenNotFound() {
        super(EXCEPTION_TYPE.TOKEN_NOT_FOUND);
    }

    public TokenNotFound(String detailMessage) {
        super(EXCEPTION_TYPE.TOKEN_NOT_FOUND, detailMessage);
    }

    public TokenNotFound(String detailMessage, Throwable throwable) {
        super(EXCEPTION_TYPE.TOKEN_NOT_FOUND, detailMessage, throwable);
    }
}
