package com.vodafone.global.sdk;

public class GenericServerError extends VodafoneException {
    public GenericServerError() {
        super(EXCEPTION_TYPE.GENERIC_SERVER_ERROR);
    }

    public GenericServerError(String detailMessage) {
        super(EXCEPTION_TYPE.GENERIC_SERVER_ERROR, detailMessage);
    }

    public GenericServerError(String detailMessage, Throwable throwable) {
        super(EXCEPTION_TYPE.GENERIC_SERVER_ERROR, detailMessage, throwable);
    }
}
