package com.vodafone.global.sdk.http.sms;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import timber.log.Timber;

public class ValidatePinRequestListener implements RequestListener<Void> {

    @Override
    public void onRequestSuccess(Void aVoid) {
    }

    @Override
    public void onRequestFailure(SpiceException e) {
        Timber.e(e, e.getMessage());
    }
}
