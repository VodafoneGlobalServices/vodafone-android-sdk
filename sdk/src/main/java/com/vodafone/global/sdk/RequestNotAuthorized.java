package com.vodafone.global.sdk;

public class RequestNotAuthorized extends VodafoneException {
    public RequestNotAuthorized() {
        super(EXCEPTION_TYPE.REQUEST_NOT_AUTHORIZED);
    }

    public RequestNotAuthorized(String detailMessage) {
        super(EXCEPTION_TYPE.REQUEST_NOT_AUTHORIZED, detailMessage);
    }

    public RequestNotAuthorized(String detailMessage, Throwable throwable) {
        super(EXCEPTION_TYPE.REQUEST_NOT_AUTHORIZED, detailMessage, throwable);
    }
}
