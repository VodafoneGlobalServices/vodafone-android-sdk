package com.vodafone.global.sdk;

public class TokenNotFound extends VodafoneException {
    public TokenNotFound() {
        super(ExceptionType.TOKEN_NOT_FOUND);
    }

    public TokenNotFound(String detailMessage) {
        super(ExceptionType.TOKEN_NOT_FOUND, detailMessage);
    }

    public TokenNotFound(String detailMessage, Throwable throwable) {
        super(ExceptionType.TOKEN_NOT_FOUND, detailMessage, throwable);
    }
}
