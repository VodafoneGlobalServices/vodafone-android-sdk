package com.vodafone.global.sdk;

public class CallThresholdReached extends VodafoneException {
    public CallThresholdReached() {
        super(ExceptionType.CALL_THRESHOLD_REACHED);
    }

    public CallThresholdReached(String detailMessage) {
        super(ExceptionType.CALL_THRESHOLD_REACHED, detailMessage);
    }

    public CallThresholdReached(ExceptionType exceptionType, String detailMessage, Throwable throwable) {
        super(ExceptionType.CALL_THRESHOLD_REACHED, detailMessage, throwable);
    }
}
