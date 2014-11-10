package com.vodafone.global.sdk;

public class NotInitialized extends VodafoneException {
    public NotInitialized() {
        super(Type.NOT_INITIALIZED, "Vodafone SDK not initialized");
    }
}
