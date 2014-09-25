package com.vodafone.global.sdk;

public class RequestValidationError extends VodafoneException {
    public RequestValidationError() {
        super(EXCEPTION_TYPE.REQUEST_VALIDATION_ERROR);
    }

    public RequestValidationError(String detailMessage) {
        super(EXCEPTION_TYPE.REQUEST_VALIDATION_ERROR, detailMessage);
    }

    public RequestValidationError(String detailMessage, Throwable throwable) {
        super(EXCEPTION_TYPE.REQUEST_VALIDATION_ERROR, detailMessage, throwable);
    }
}
