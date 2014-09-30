package com.vodafone.global.sdk.http.worker;

import com.squareup.okhttp.Response;
import com.vodafone.global.sdk.GenericServerError;
import com.vodafone.global.sdk.RequestValidationError;
import com.vodafone.global.sdk.ResolveCallbacks;
import com.vodafone.global.sdk.TokenNotFound;
import com.vodafone.global.sdk.http.HttpCode;
import com.vodafone.global.sdk.http.parser.Parsers;
import org.json.JSONException;

import java.io.IOException;

import static com.vodafone.global.sdk.http.HttpCode.*;

public class ValidatePinParser  {
    private final ResolveCallbacks resolveCallbacks;

    public ValidatePinParser(ResolveCallbacks resolveCallbacks) {
        this.resolveCallbacks = resolveCallbacks;
    }

    void parseResponse(Response response) throws IOException, JSONException {
        int code = response.code();
        switch (code) {
            case HttpCode.OK_200:
                resolveCallbacks.notifyUserDetailUpdate(Parsers.parseUserDetails(response));
                // TODO ValidateSmsCallback.onSmsValidationSuccessful()
                break;
            case BAD_REQUEST_400:
                // TODO verify behaviour with flow diagram
                resolveCallbacks.notifyError(new RequestValidationError());
                break;
            case FORBIDDEN_403:
                resolveCallbacks.notifyError(new TokenNotFound());
                break;
            case NOT_FOUND_404:
                resolveCallbacks.notifyError(new TokenNotFound());
                break;
            case CONFLICT_409:
                // TODO pin validated failed
                // if (intercepts)
                //   com.vodafone.global.sdk.ResolutionCallback.onFailed()
                // else
                //   com.vodafone.global.sdk.ValidateSmsCallback.onSmsValidationError()
                break;
            default:
                resolveCallbacks.notifyError(new GenericServerError());
        }
    }
}
