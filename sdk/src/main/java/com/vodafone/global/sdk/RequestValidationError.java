package com.vodafone.global.sdk;

public class RequestValidationError extends VodafoneException {
    public RequestValidationError() {
        super(ExceptionType.REQUEST_VALIDATION_ERROR);
    }

    public RequestValidationError(String detailMessage) {
        super(ExceptionType.REQUEST_VALIDATION_ERROR, detailMessage);
    }

    public RequestValidationError(String detailMessage, Throwable throwable) {
        super(ExceptionType.REQUEST_VALIDATION_ERROR, detailMessage, throwable);
    }
}
