package com.vodafone.global.sdk;

public class InternalSdkError extends VodafoneException {

    public InternalSdkError() {
        super(ExceptionType.INTERNAL_SDK_ERROR);
    }

    public InternalSdkError(String detailMessage) {
        super(ExceptionType.INTERNAL_SDK_ERROR, detailMessage);
    }

    public InternalSdkError(String detailMessage, Throwable throwable) {
        super(ExceptionType.INTERNAL_SDK_ERROR, detailMessage, throwable);
    }
}
