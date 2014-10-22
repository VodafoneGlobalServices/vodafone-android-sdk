package com.vodafone.global.sdk;

public class CallThresholdReached extends VodafoneException {
    public CallThresholdReached() {
        super(Type.THROTTLING_LIMIT_EXCEEDED);
    }

    public CallThresholdReached(String detailMessage) {
        super(Type.THROTTLING_LIMIT_EXCEEDED, detailMessage);
    }

    public CallThresholdReached(Type type, String detailMessage, Throwable throwable) {
        super(Type.THROTTLING_LIMIT_EXCEEDED, detailMessage, throwable);
    }
}
