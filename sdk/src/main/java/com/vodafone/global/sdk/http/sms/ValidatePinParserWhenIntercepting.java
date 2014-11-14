package com.vodafone.global.sdk.http.sms;

import com.vodafone.global.sdk.*;
import com.vodafone.global.sdk.http.GenericServerError;
import com.vodafone.global.sdk.http.HttpCode;
import com.vodafone.global.sdk.http.ResponseHolder;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import static com.vodafone.global.sdk.MessageType.AUTHENTICATE;
import static com.vodafone.global.sdk.MessageType.VALIDATE_PIN;
import static com.vodafone.global.sdk.http.HttpCode.*;

public class ValidatePinParserWhenIntercepting {
    private final Worker worker;
    private final ResolveCallbacks resolveCallbacks;
    private final ValidateSmsCallbacks validateSmsCallbacks;

    public ValidatePinParserWhenIntercepting(
            Worker worker,
            ResolveCallbacks resolveCallbacks,
            ValidateSmsCallbacks validateSmsCallbacks
    ) {
        this.worker = worker;
        this.resolveCallbacks = resolveCallbacks;
        this.validateSmsCallbacks = validateSmsCallbacks;
    }

    void parseResponse(ResponseHolder response, String token, String pin) throws IOException, JSONException {

    }
}
