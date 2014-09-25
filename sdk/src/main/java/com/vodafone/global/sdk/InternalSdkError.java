package com.vodafone.global.sdk;

public class InternalSdkError extends VodafoneException {

    public InternalSdkError() {
        super(EXCEPTION_TYPE.INTERNAL_SDK_ERROR);
    }

    public InternalSdkError(String detailMessage) {
        super(EXCEPTION_TYPE.INTERNAL_SDK_ERROR, detailMessage);
    }

    public InternalSdkError(String detailMessage, Throwable throwable) {
        super(EXCEPTION_TYPE.INTERNAL_SDK_ERROR, detailMessage, throwable);
    }
}
