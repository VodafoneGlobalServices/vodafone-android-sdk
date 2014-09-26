package com.vodafone.global.sdk;

public class NoInternetConnection extends VodafoneException {

    public NoInternetConnection() {
        super(ExceptionType.NO_INTERNET_CONNECTION);
    }

    public NoInternetConnection(String detailMessage) {
        super(ExceptionType.NO_INTERNET_CONNECTION, detailMessage);
    }

    public NoInternetConnection(String detailMessage, Throwable throwable) {
        super(ExceptionType.NO_INTERNET_CONNECTION, detailMessage, throwable);
    }
}
