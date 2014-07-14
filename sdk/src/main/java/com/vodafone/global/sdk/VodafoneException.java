package com.vodafone.global.sdk;

public class VodafoneException extends Exception {
    public VodafoneException() {
        super();
    }

    public VodafoneException(String detailMessage) {
        super(detailMessage);
    }

    public VodafoneException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}
