package com.vodafone.global.sdk.http.sms;

import com.squareup.okhttp.Response;
import com.vodafone.global.sdk.ResolveCallbacks;
import com.vodafone.global.sdk.UserDetails;
import com.vodafone.global.sdk.ValidateSmsCallbacks;
import com.vodafone.global.sdk.http.GenericServerError;
import com.vodafone.global.sdk.http.HttpCode;
import org.json.JSONException;

import java.io.IOException;

import static com.vodafone.global.sdk.http.HttpCode.*;

public class ValidatePinParser  {
    private final ResolveCallbacks resolveCallbacks;
    private final ValidateSmsCallbacks validateSmsCallbacks;
    private final boolean intercepts = false; // TODO

    public ValidatePinParser(
            ResolveCallbacks resolveCallbacks,
            ValidateSmsCallbacks validateSmsCallbacks
    ) {
        this.resolveCallbacks = resolveCallbacks;
        this.validateSmsCallbacks = validateSmsCallbacks;
    }

    void parseResponse(Response response, String token) throws IOException, JSONException {
        int code = response.code();
        switch (code) {
            case HttpCode.OK_200:
                resolveCallbacks.completed(UserDetails.fromJson(response.body().string()));
                validateSmsCallbacks.notifySuccess();
                break;
            case BAD_REQUEST_400:
                resolveCallbacks.notifyError(new InvalidInput());
                break;
            case FORBIDDEN_403:
                resolveCallbacks.validationRequired(token);
                break;
            case NOT_FOUND_404:
                resolveCallbacks.unableToResolve();
                break;
            case CONFLICT_409:
                if (intercepts)
                   resolveCallbacks.validationRequired(token);
                else
                   validateSmsCallbacks.notifyFailure();
                break;
            default:
                resolveCallbacks.notifyError(new GenericServerError());
        }
    }
}
