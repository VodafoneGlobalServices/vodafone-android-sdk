package com.vodafone.global.sdk;

public class InvalidMsisdn extends VodafoneException {
    public InvalidMsisdn() {
        super(ExceptionType.INVALID_MSISDN);
    }

    public InvalidMsisdn(String detailMessage) {
        super(ExceptionType.INVALID_MSISDN, detailMessage);
    }

    public InvalidMsisdn(String detailMessage, Throwable throwable) {
        super(ExceptionType.INVALID_MSISDN, detailMessage, throwable);
    }
}
