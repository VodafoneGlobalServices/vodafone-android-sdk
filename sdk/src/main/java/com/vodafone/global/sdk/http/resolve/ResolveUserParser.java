package com.vodafone.global.sdk.http.resolve;

import android.content.Context;
import com.vodafone.global.sdk.ResolveCallbacks;
import com.vodafone.global.sdk.UserDetails;
import com.vodafone.global.sdk.Worker;
import com.vodafone.global.sdk.http.GenericServerError;
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

public class ResolveUserParser {
    private final Worker worker;
    private final Context context;
    private final ResolveCallbacks resolveCallbacks;

    public ResolveUserParser(Worker worker, Context context, ResolveCallbacks resolveCallbacks) {
        this.worker = worker;
        this.context = context;
        this.resolveCallbacks = resolveCallbacks;
    }

    public void parseResponse(ResponseHolder response) throws IOException, JSONException {
        int code = response.code();
        switch (code) {
            case CREATED_201:
                resolveCallbacks.completed(UserDetails.fromJson(response.body()));
                break;
            case FOUND_302:
                String location = response.header("Location");
                String token = extractToken(location);
                if (requiresSmsValidation(location)) {
                    if (canReadSMS()) {
                        resolveCallbacks.setSessionToken(token);
                        generatePin(token);
                    } else {
                        validationRequired(token);
                    }
                } else if (resolutionIsOngoing(location)) {
                    int retryAfter = Integer.valueOf(response.header("Retry-After", "1000"));
                    checkStatus(token, retryAfter);
                } else {
                    resolveCallbacks.notifyError(new GenericServerError());
                }
                break;
            case NOT_FOUND_404:
                resolveCallbacks.unableToResolve();
                break;
            case BAD_REQUEST_400:
                resolveCallbacks.notifyError(new GenericServerError("Application ID doesn't exist on APIX" +
                        " OR Application ID has no seamlessID scope associated"));
                break;
            case FORBIDDEN_403:
                String body = response.body();
                if (!body.isEmpty()) {
                    JSONObject json = new JSONObject(response.body());
                    String id = json.getString("id");
                    if (id.equals("POL0002")) {
                        worker.sendMessage(worker.createMessage(AUTHENTICATE));
                        worker.sendMessage(worker.createMessage(RETRIEVE_USER_DETAILS));
                    }
                } else {
                    resolveCallbacks.notifyError(new GenericServerError());
                }
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
        String regex = ".*/users/tokens/([^/^?]*).*";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(location);
        if (!matcher.matches()) throw new IllegalStateException("can't extract token from 'Location' header");
        return matcher.group(1);
    }

    private void validationRequired(String token) {
        resolveCallbacks.validationRequired(token);
    }

    private boolean resolutionIsOngoing(String location) {
        Pattern pattern = Pattern.compile(".*/users/tokens/[^/]*\\?backendId=.*");
        Matcher matcher = pattern.matcher(location);
        return matcher.matches();
    }

    private void checkStatus(String tokenId, int retryAfter) {
        worker.sendMessageDelayed(worker.createMessage(CHECK_STATUS, new CheckStatusParameters(tokenId)), retryAfter);
    }
}
