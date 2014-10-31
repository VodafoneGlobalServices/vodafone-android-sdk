package com.vodafone.global.sdk.http.sms;

import com.vodafone.global.sdk.ResolveCallbacks;
import com.vodafone.global.sdk.ValidateSmsCallbacks;
import com.vodafone.global.sdk.http.GenericServerError;
import com.vodafone.global.sdk.http.ResponseHolder;

import static com.vodafone.global.sdk.http.HttpCode.*;

public class GeneratePinParser {
    private final ResolveCallbacks resolveCallbacks;
    private final ValidateSmsCallbacks validateSmsCallbacks;

    public GeneratePinParser(
            ResolveCallbacks resolveCallbacks,
            ValidateSmsCallbacks validateSmsCallbacks
    ) {
        this.resolveCallbacks = resolveCallbacks;
        this.validateSmsCallbacks = validateSmsCallbacks;
    }

    void parseResponse(ResponseHolder response) {
        int code = response.code();
        switch (code) {
            case OK_200:
                validateSmsCallbacks.notifySuccess();
                break;
            case BAD_REQUEST_400:
            case FORBIDDEN_403:
                validateSmsCallbacks.notifyError(new InvalidInput());
                break;
            case NOT_FOUND_404:
                resolveCallbacks.unableToResolve();
                break;
            default:
                validateSmsCallbacks.notifyError(new GenericServerError("unknown http code " + code));
        }
    }
}
