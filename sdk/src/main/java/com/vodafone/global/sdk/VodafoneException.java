package com.vodafone.global.sdk;

public abstract class VodafoneException extends RuntimeException {
    public enum Type {
        GENERIC_SERVER_ERROR("Generic server failure"),
        THROTTLING_LIMIT_EXCEEDED("Call threshold reached"),
        NO_INTERNET_CONNECTION("No internet connection"),
        CONNECTION_TIMEOUT("Connection timeout"),
        INVALID_INPUT("Invalid input"),
        RESOLUTION_TIMEOUT("Resolution timeout"),
        WRONG_SMS_CODE("Wrong sms code"),
        AUTHORIZATION_FAILED("Authorization failed"),
        OPERATOR_NOT_SUPPORTED("Operator not supported"),
        NOT_INITIALIZED("Not initialized");

        private final String message;

        Type(String message) {
            this.message = message;
        }
    }

    private final Type type;

    public Type getType() {
        return type;
    }

    public VodafoneException(Type type) {
        super();
        this.type = type;
    }

    public VodafoneException(Type type, Throwable t) {
        super(t);
        this.type = type;
    }

    public VodafoneException(Type type, String detailMessage) {
        super(detailMessage);
        this.type = type;
    }

    public VodafoneException(Type type, String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
        this.type = type;
    }
}
