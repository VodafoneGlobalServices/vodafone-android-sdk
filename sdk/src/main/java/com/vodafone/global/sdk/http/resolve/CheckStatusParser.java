package com.vodafone.global.sdk.http.resolve;

import android.content.Context;
import android.os.Message;
import com.vodafone.global.sdk.ResolveCallbacks;
import com.vodafone.global.sdk.UserDetails;
import com.vodafone.global.sdk.Worker;
import com.vodafone.global.sdk.http.GenericServerError;
import com.vodafone.global.sdk.http.HttpCode;
import com.vodafone.global.sdk.http.ResponseHolder;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.Manifest.permission.RECEIVE_SMS;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.vodafone.global.sdk.MessageType.*;
import static com.vodafone.global.sdk.http.HttpCode.*;

public class CheckStatusParser {
    private final ResolveCallbacks resolveCallbacks;
    private final Context context;
    private final Worker worker;

    public CheckStatusParser(Worker worker, Context context, ResolveCallbacks resolveCallbacks) {
        this.worker = worker;
        this.resolveCallbacks = resolveCallbacks;
        this.context = context;
    }

    void parseResponse(ResponseHolder response, CheckStatusParameters parameters) throws IOException, JSONException {
        int code = response.code();
        switch (code) {
            case OK_200:
                resolveCallbacks.completed(UserDetails.fromJson(response.body()));
                break;
            case FOUND_302:
                String location = response.header("Location");
                if (requiresSmsValidation(location)) {
                    if (canReadSMS()) {
                        generatePin(extractToken(location));
                    } else {
                        resolveCallbacks.validationRequired(extractToken(location));
                    }
                } else {
                    int retryAfter = Integer.valueOf(response.header("Retry-After", "500"));
                    String etag = response.header("Etag");
                    CheckStatusParameters newParameters = new CheckStatusParameters(
                            parameters.tokenId.get(), etag, retryAfter);
                    Message message = worker.createMessage(CHECK_STATUS, newParameters);
                    worker.sendMessageDelayed(message, retryAfter);
                }
                break;
            case HttpCode.NOT_MODIFIED_304:
                int retryAfter = Integer.valueOf(response.header("RetryAfter", "500"));
                CheckStatusParameters newParameters = new CheckStatusParameters(
                        parameters.tokenId.get(), parameters.etag.get(), retryAfter);
                Message message = worker.createMessage(CHECK_STATUS, newParameters);
                worker.sendMessageDelayed(message, newParameters.retryAfter.get());
                break;
            case BAD_REQUEST_400:
                resolveCallbacks.unableToResolve();
                break;
            case FORBIDDEN_403:
                String body = response.body();
                if (!body.isEmpty()) {
                    try {
                        JSONObject json = new JSONObject(body);
                        String id = json.getString("id");
                        if (id.equals("POL0002")) {
                            worker.sendMessage(worker.createMessage(AUTHENTICATE));
                            worker.sendMessage(worker.createMessage(CHECK_STATUS, parameters));
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
            default:
                resolveCallbacks.notifyError(new GenericServerError());
        }
    }

    protected boolean requiresSmsValidation(String location) {
        Pattern pattern = Pattern.compile(".*/users/tokens/[^/]*/pins\\?backendId=.*");
        Matcher matcher = pattern.matcher(location);
        return matcher.matches();
    }

    protected boolean canReadSMS() {
        return context.checkCallingOrSelfPermission(RECEIVE_SMS) == PERMISSION_GRANTED;
    }

    protected void generatePin(String token) {
        worker.sendMessage(worker.createMessage(GENERATE_PIN, token));
    }

    protected String extractToken(String location) {
        Pattern pattern = Pattern.compile(".*/users/tokens/(.*)[/?].*");
        Matcher matcher = pattern.matcher(location);
        return matcher.group(1);
    }
}
