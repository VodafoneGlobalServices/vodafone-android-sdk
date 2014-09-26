package com.vodafone.global.sdk;

public abstract class VodafoneException extends RuntimeException {
    public enum ExceptionType {
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

        ExceptionType(String message) {
            this.message = message;
        }
    }
    private ExceptionType exceptionType;

    public ExceptionType getExceptionType() {
        return exceptionType;
    }

    public VodafoneException(ExceptionType exceptionType) {
        super();
        this.exceptionType = exceptionType;
    }

    public VodafoneException(ExceptionType exceptionType, String detailMessage) {
        super(detailMessage);
        this.exceptionType = exceptionType;
    }

    public VodafoneException(ExceptionType exceptionType, String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
        this.exceptionType = exceptionType;
    }
}
