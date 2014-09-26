package com.vodafone.global.sdk;

public class RequestNotAuthorized extends VodafoneException {
    public RequestNotAuthorized() {
        super(ExceptionType.REQUEST_NOT_AUTHORIZED);
    }

    public RequestNotAuthorized(String detailMessage) {
        super(ExceptionType.REQUEST_NOT_AUTHORIZED, detailMessage);
    }

    public RequestNotAuthorized(String detailMessage, Throwable throwable) {
        super(ExceptionType.REQUEST_NOT_AUTHORIZED, detailMessage, throwable);
    }
}
