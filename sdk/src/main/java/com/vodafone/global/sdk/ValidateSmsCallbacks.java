package com.vodafone.global.sdk;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class ValidateSmsCallbacks {
    Set<ValidateSmsCallback> validateSmsCallbacks = new CopyOnWriteArraySet<ValidateSmsCallback>();

    public void add(ValidateSmsCallback callback) {
        validateSmsCallbacks.add(callback);
    }

    public void remove(ValidateSmsCallback callback) {
        validateSmsCallbacks.remove(callback);
    }

    public void notifySuccess() {
        // TODO callback call on main thread
        for (ValidateSmsCallback callback : validateSmsCallbacks) {
            callback.onPinGenerationSuccess();
        }
    }

    public void notifyError(VodafoneException vodafoneException) {
        // TODO callback call on main thread
        for (ValidateSmsCallback callback : validateSmsCallbacks) {
            callback.onSmsValidationError(vodafoneException);
        }
    }
}
