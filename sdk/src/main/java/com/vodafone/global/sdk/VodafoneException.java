package com.vodafone.global.sdk;

public abstract class VodafoneException extends RuntimeException {
    public enum EXCEPTION_TYPE {
        TOKEN_NOT_FOUND(""),
        GENERIC_SERVER_ERROR("Generic server failure"),
        INTERNAL_SDK_ERROR("Internal SDK error"),
        REQUEST_VALIDATION_ERROR("Request validation error"),
        REQUEST_NOT_AUTHORIZED("Request not authorized"),
        WRONG_OTP_PROVIDED("Wrong OTP provided"),
        INVALID_OTP_FORMAT("Invalid OTP format"),
        INVALID_TOKEN_STATE("Invalid token state"),
        INVALID_MSISDN("Invalid MSISDN"),
        CALL_THRESHOLD_REACHED("Call threshold reached");

        private final String message;

        EXCEPTION_TYPE(String message) {
            this.message = message;
        }
    }
    private EXCEPTION_TYPE exceptionType;

    public EXCEPTION_TYPE getExceptionType() {
        return exceptionType;
    }

    public VodafoneException(EXCEPTION_TYPE exceptionType) {
        super();
        this.exceptionType = exceptionType;
    }

    public VodafoneException(EXCEPTION_TYPE exceptionType, String detailMessage) {
        super(detailMessage);
        this.exceptionType = exceptionType;
    }

    public VodafoneException(EXCEPTION_TYPE exceptionType, String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
        this.exceptionType = exceptionType;
    }
}
