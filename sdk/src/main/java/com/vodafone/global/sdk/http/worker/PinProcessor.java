package com.vodafone.global.sdk.http.worker;

import android.content.Context;
import android.os.Message;

import com.google.common.base.Optional;
import com.vodafone.global.sdk.Settings;
import com.vodafone.global.sdk.ValidateSmsCallback;
import com.vodafone.global.sdk.VodafoneException;
import com.vodafone.global.sdk.http.oauth.OAuthToken;

import java.util.Set;

public abstract class PinProcessor {
    protected final Settings settings;
    protected final Context context;
    private final Set<ValidateSmsCallback> validateSmsCallbacks;


    public PinProcessor(Context context, Settings settings, Set<ValidateSmsCallback> validateSmsCallbacks) {
        this.context = context;
        this.settings = settings;
        this.validateSmsCallbacks = validateSmsCallbacks;
    }

    abstract void process(Worker worker, Optional<OAuthToken> authToken, Message msg);

    protected void notifySuccess() {
        //validateSmsCallbacks;
    }

    protected void notifyError(VodafoneException vodafoneException) {
        for (ValidateSmsCallback callback : validateSmsCallbacks) {
            callback.onSmsValidationError(vodafoneException);
        }
    }
}
