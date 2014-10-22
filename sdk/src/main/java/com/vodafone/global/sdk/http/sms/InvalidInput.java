package com.vodafone.global.sdk.http.sms;

import com.vodafone.global.sdk.VodafoneException;

public class InvalidInput extends VodafoneException {
    public InvalidInput() {
        super(Type.INVALID_INPUT);
    }

    public InvalidInput(String detailMessage) {
        super(Type.INVALID_INPUT, detailMessage);
    }

    public InvalidInput(String detailMessage, Throwable throwable) {
        super(Type.INVALID_INPUT, detailMessage, throwable);
    }
}
