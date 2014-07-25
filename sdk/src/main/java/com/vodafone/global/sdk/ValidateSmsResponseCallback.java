package com.vodafone.global.sdk;

import android.util.Log;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Set;

class ValidateSmsResponseCallback implements Callback {
    private static final String TAG = ValidateSmsResponseCallback.class.getSimpleName();
    private final Set<ValidateSmsCallback> validateSmsCallbacks;

    public ValidateSmsResponseCallback(Set<ValidateSmsCallback> validateSmsCallbacks) {
        this.validateSmsCallbacks = validateSmsCallbacks;
    }

    @Override
    public void onFailure(Request request, IOException e) {
        for (ValidateSmsCallback callback : validateSmsCallbacks) {
            callback.onSmsValidationError(new VodafoneException(e.getMessage(), e));
        }
    }

    @Override
    public void onResponse(Response response) throws IOException {
        final int httpCode = response.code();
        switch (httpCode) {
            case HttpStatus.SC_OK:
                // sms validation successful
                for (ValidateSmsCallback callback : validateSmsCallbacks) {
                    callback.onSmsValidationSuccessful();
                }
                break;
            case HttpStatus.SC_BAD_REQUEST:
                // sms validation failed
                try {
                    JSONObject json = new JSONObject(response.body().string());
                    String error = json.getString("error");
                    String errorMessage = json.getString("errorMessage");

                    Log.e(TAG, error + ": " + errorMessage);

                    for (ValidateSmsCallback callback : validateSmsCallbacks) {
                        callback.onSmsValidationFailure();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
                break;
        }
    }
}
