package com.vodafone.global.sdk.http;

import com.vodafone.global.sdk.VodafoneException;

public class BadRequest extends VodafoneException {
    public BadRequest() {
        super(ExceptionType.BAD_REQUEST);
    }

    public BadRequest(String detailMessage) {
        super(ExceptionType.BAD_REQUEST, detailMessage);
    }

    public BadRequest(String detailMessage, Throwable throwable) {
        super(ExceptionType.BAD_REQUEST, detailMessage, throwable);
    }
}
