package com.vodafone.global.sdk;

public class InvalidMsisdn extends VodafoneException {
    public InvalidMsisdn() {
        super(EXCEPTION_TYPE.INVALID_MSISDN);
    }

    public InvalidMsisdn(String detailMessage) {
        super(EXCEPTION_TYPE.INVALID_MSISDN, detailMessage);
    }

    public InvalidMsisdn(String detailMessage, Throwable throwable) {
        super(EXCEPTION_TYPE.INVALID_MSISDN, detailMessage, throwable);
    }
}
