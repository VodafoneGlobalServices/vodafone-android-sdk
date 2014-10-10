package com.vodafone.global.sdk.http;

import com.vodafone.global.sdk.VodafoneException;

public class GenericServerError extends VodafoneException {
    public GenericServerError() {
        super(ExceptionType.GENERIC_SERVER_ERROR);
    }

    public GenericServerError(Throwable throwable) {
        super(ExceptionType.GENERIC_SERVER_ERROR, throwable);
    }

    public GenericServerError(String detailMessage) {
        super(ExceptionType.GENERIC_SERVER_ERROR, detailMessage);
    }

    public GenericServerError(String detailMessage, Throwable throwable) {
        super(ExceptionType.GENERIC_SERVER_ERROR, detailMessage, throwable);
    }
}
