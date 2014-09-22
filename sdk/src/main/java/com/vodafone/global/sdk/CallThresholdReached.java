package com.vodafone.global.sdk;

public class CallThresholdReached extends VodafoneException {
    public CallThresholdReached() {
        super(EXCEPTION_TYPE.CALL_THRESHOLD_REACHED);
    }

    public CallThresholdReached(String detailMessage) {
        super(EXCEPTION_TYPE.CALL_THRESHOLD_REACHED, detailMessage);
    }

    public CallThresholdReached(EXCEPTION_TYPE exceptionType, String detailMessage, Throwable throwable) {
        super(EXCEPTION_TYPE.CALL_THRESHOLD_REACHED, detailMessage, throwable);
    }
}
