package com.vodafone.global.sdk.http;

import com.vodafone.global.sdk.VodafoneException;

public class ResolutionTimeout extends VodafoneException {
    public ResolutionTimeout() {
        super(Type.RESOLUTION_TIMEOUT);
    }
}
