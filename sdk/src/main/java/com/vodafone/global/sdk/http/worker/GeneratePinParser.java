package com.vodafone.global.sdk.http.worker;

import com.squareup.okhttp.Response;
import com.vodafone.global.sdk.GenericServerError;
import com.vodafone.global.sdk.RequestValidationError;
import com.vodafone.global.sdk.TokenNotFound;
import com.vodafone.global.sdk.ValidateSmsCallbacks;

import static com.vodafone.global.sdk.http.HttpCode.*;

public class GeneratePinParser {
    private final ValidateSmsCallbacks validateSmsCallbacks;

    public GeneratePinParser(ValidateSmsCallbacks validateSmsCallbacks) {

        this.validateSmsCallbacks = validateSmsCallbacks;
    }

    void parseResponse(Response response) {
        int code = response.code();
        switch (code) {
            case OK_200:
                validateSmsCallbacks.notifySuccess();
                break;
            case BAD_REQUEST_400:
            case FORBIDDEN_403:
                // TODO validate error, error invalid input
                validateSmsCallbacks.notifyError(new RequestValidationError());
                break;
            case NOT_FOUND_404: // TODO
                // TODO notify user details callback about
                // TODO validate error
                validateSmsCallbacks.notifyError(new TokenNotFound());
                break;
            default:
                validateSmsCallbacks.notifyError(new GenericServerError());
        }
    }
}
