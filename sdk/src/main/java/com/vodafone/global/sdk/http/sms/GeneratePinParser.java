package com.vodafone.global.sdk.http.sms;

import com.vodafone.global.sdk.ResolveCallbacks;
import com.vodafone.global.sdk.ValidateSmsCallbacks;
import com.vodafone.global.sdk.Worker;
import com.vodafone.global.sdk.http.GenericServerError;
import com.vodafone.global.sdk.http.ResponseHolder;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import static com.vodafone.global.sdk.MessageType.AUTHENTICATE;
import static com.vodafone.global.sdk.MessageType.GENERATE_PIN;
import static com.vodafone.global.sdk.http.HttpCode.*;

public class GeneratePinParser {
    private final Worker worker;
    private final ResolveCallbacks resolveCallbacks;
    private final ValidateSmsCallbacks validateSmsCallbacks;

    public GeneratePinParser(
            Worker worker,
            ResolveCallbacks resolveCallbacks,
            ValidateSmsCallbacks validateSmsCallbacks
    ) {
        this.worker = worker;
        this.resolveCallbacks = resolveCallbacks;
        this.validateSmsCallbacks = validateSmsCallbacks;
    }

    void parseResponse(ResponseHolder response, String token) throws IOException {
        int code = response.code();
        switch (code) {
            case OK_200:
                validateSmsCallbacks.notifyPinGenerationSuccess();
                break;
            case BAD_REQUEST_400:
                validateSmsCallbacks.notifyError(new InvalidInput());
            case FORBIDDEN_403:
                String body = response.body();
                if (body != null && !body.isEmpty()) {
                    try {
                        JSONObject json = new JSONObject(body);
                        String id = json.getString("id");
                        if (id.equals("POL0002")) {
                            worker.sendMessage(worker.createMessage(AUTHENTICATE));
                            worker.sendMessage(worker.createMessage(GENERATE_PIN, token));
                        }
                    } catch (JSONException e) {
                        resolveCallbacks.notifyError(new GenericServerError("Unrecognized HTTP 403 on check status, " +
                                "body: '" + body + "'"));
                    }
                } else {
                    validateSmsCallbacks.notifyError(new InvalidInput());
                }
                break;
            case NOT_FOUND_404:
                resolveCallbacks.unableToResolve();
                break;
            default:
                validateSmsCallbacks.notifyError(new GenericServerError("unknown http code " + code));
        }
    }
}
