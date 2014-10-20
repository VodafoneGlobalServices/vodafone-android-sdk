package com.vodafone.global.sdk;

import android.os.Handler;
import android.os.Looper;

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
        Handler handler = new Handler(Looper.getMainLooper());
        for (final ValidateSmsCallback callback : validateSmsCallbacks) {
            handler.post(new Runnable() {
                public void run() {
                    callback.onPinGenerationSuccess();
                }
            });
        }
    }

    public void notifyError(final VodafoneException vodafoneException) {
        Handler handler = new Handler(Looper.getMainLooper());
        for (final ValidateSmsCallback callback : validateSmsCallbacks) {
            handler.post(new Runnable() {
                public void run() {
                    callback.onSmsValidationError(vodafoneException);
                }
            });
        }
    }
}
