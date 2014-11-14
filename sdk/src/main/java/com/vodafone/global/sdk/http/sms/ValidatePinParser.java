package com.vodafone.global.sdk.http.sms;

import com.vodafone.global.sdk.*;
import com.vodafone.global.sdk.http.GenericServerError;
import com.vodafone.global.sdk.http.HttpCode;
import com.vodafone.global.sdk.http.ResolutionTimeout;
import com.vodafone.global.sdk.http.ResponseHolder;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import static com.vodafone.global.sdk.MessageType.AUTHENTICATE;
import static com.vodafone.global.sdk.MessageType.VALIDATE_PIN;
import static com.vodafone.global.sdk.http.HttpCode.*;

public abstract class ValidatePinParser  {
    protected final Worker worker;
    protected final ResolveCallbacks resolveCallbacks;
    protected final ValidateSmsCallbacks validateSmsCallbacks;

    private ValidatePinParser(
            Worker worker,
            ResolveCallbacks resolveCallbacks,
            ValidateSmsCallbacks validateSmsCallbacks
    ) {
        this.worker = worker;
        this.resolveCallbacks = resolveCallbacks;
        this.validateSmsCallbacks = validateSmsCallbacks;
    }

    public abstract void parseResponse(ResponseHolder response, String token, String pin) throws IOException, JSONException;

    public static ValidatePinParser withoutInterception(
            Worker worker,
            ResolveCallbacks resolveCallbacks,
            ValidateSmsCallbacks validateSmsCallbacks
    ) {
        return new ValidatePinParser(worker, resolveCallbacks, validateSmsCallbacks) {
            @Override
            public void parseResponse(ResponseHolder response, String token, String pin) throws IOException, JSONException {
                int code = response.code();
                switch (code) {
                    case HttpCode.OK_200:
                        resolveCallbacks.completed(UserDetails.fromJson(response.body()));
                        validateSmsCallbacks.notifySuccess();
                        break;
                    case BAD_REQUEST_400:
                        resolveCallbacks.notifyError(new InvalidInput());
                        break;
                    case FORBIDDEN_403:
                        String body = response.body();
                        if (body!= null && !body.isEmpty()) {
                            try {
                                JSONObject json = new JSONObject(body);
                                String id = json.getString("id");
                                if (id.equals("POL0002")) {
                                    worker.sendMessage(worker.createMessage(AUTHENTICATE));
                                    ValidatePinParameters parameters = ValidatePinParameters.builder()
                                            .token(token)
                                            .pin(pin)
                                            .build();
                                    worker.sendMessage(worker.createMessage(VALIDATE_PIN, parameters));
                                }
                            } catch (JSONException e) {
                                resolveCallbacks.notifyError(new GenericServerError("Unrecognized HTTP 403 on check status, " +
                                        "body: '" + body + "'"));
                            }
                        } else {
                            resolveCallbacks.notifyError(new GenericServerError("Unrecognized HTTP 403 on check status, " +
                                    "body: '" + body + "'"));
                        }
                        break;
                    case NOT_FOUND_404:
                        resolveCallbacks.notifyError(new ResolutionTimeout());
                        break;
                    case CONFLICT_409:
                        validateSmsCallbacks.notifyError(new InvalidInput());
                        break;
                    default:
                        resolveCallbacks.notifyError(new GenericServerError());
                }
            }
        };
    }

    public static ValidatePinParser withInterception(
            Worker worker,
            ResolveCallbacks resolveCallbacks,
            ValidateSmsCallbacks validateSmsCallbacks
    ) {
        return new ValidatePinParser(worker, resolveCallbacks, validateSmsCallbacks) {
            @Override
            public void parseResponse(ResponseHolder response, String token, String pin) throws IOException, JSONException {
                int code = response.code();
                switch (code) {
                    case HttpCode.OK_200:
                        resolveCallbacks.completed(UserDetails.fromJson(response.body()));
                        break;
                    case BAD_REQUEST_400:
                        resolveCallbacks.validationRequired(token);
                        break;
                    case FORBIDDEN_403:
                        String body = response.body();
                        if (body!= null && !body.isEmpty()) {
                            try {
                                JSONObject json = new JSONObject(body);
                                String id = json.getString("id");
                                if (id.equals("POL0002")) {
                                    worker.sendMessage(worker.createMessage(AUTHENTICATE));
                                    ValidatePinParameters parameters = ValidatePinParameters.builder()
                                            .token(token)
                                            .pin(pin)
                                            .build();
                                    worker.sendMessage(worker.createMessage(VALIDATE_PIN, parameters));
                                }
                            } catch (JSONException e) {
                                resolveCallbacks.notifyError(new GenericServerError("Unrecognized HTTP 403 on check status, " +
                                        "body: '" + body + "'"));
                            }
                        } else {
                            resolveCallbacks.notifyError(new GenericServerError("Unrecognized HTTP 403 on check status, " +
                                    "body: '" + body + "'"));
                        }
                        break;
                    case NOT_FOUND_404:
                        resolveCallbacks.unableToResolve();
                        break;
                    case CONFLICT_409:
                        resolveCallbacks.validationRequired(token);
                        break;
                    default:
                        resolveCallbacks.notifyError(new GenericServerError());
                }
            }
        };
    }
}
