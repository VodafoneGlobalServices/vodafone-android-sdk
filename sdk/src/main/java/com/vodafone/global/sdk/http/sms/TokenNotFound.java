package com.vodafone.global.sdk.http.sms;

import com.vodafone.global.sdk.VodafoneException;

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
