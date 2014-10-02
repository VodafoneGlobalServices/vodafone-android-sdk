package com.vodafone.global.sdk.http.worker;

import android.content.Context;
import com.squareup.okhttp.Response;
import com.vodafone.global.sdk.BadRequest;
import com.vodafone.global.sdk.GenericServerError;
import com.vodafone.global.sdk.ResolutionStatus;
import com.vodafone.global.sdk.ResolveCallbacks;
import com.vodafone.global.sdk.http.parser.Parsers;
import com.vodafone.global.sdk.http.resolve.UserDetailsDTO;
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

    public void parseResponse(Response response) throws IOException, JSONException {
        int code = response.code();
        switch (code) {
            case CREATED_201:
                resolveCallbacks.notifyUserDetailUpdate(Parsers.resolutionCompleted(response));
                break;
            case FOUND_302:
                String location = response.header("Location");
                if (requiresSmsValidation(location)) {
                    if (canReadSMS()) {
                        generatePin(extractToken(location));
                    } else {
                        validationRequired(extractToken(location));
                    }
                } else if (resolutionIsOngoing(location)) {
                    checkStatus();
                } else {
                    resolveCallbacks.notifyError(new GenericServerError());
                }
                break;
            case NOT_FOUND_404:
                resolutionFailed();
                break;
            case BAD_REQUEST_400:
                // Application ID doesn't exist on APIX
                // Application ID has no seamlessID scope associated
                resolveCallbacks.notifyError(new BadRequest());
                break;
            case FORBIDDEN_403:
                String body = response.body().string();
                if (!body.isEmpty()) {
                    JSONObject json = new JSONObject(response.body().string());
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
        String regex = ".*/users/tokens/(.*)[/]?.*";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(location);
        return matcher.group(1);
    }

    protected void validationRequired(String token) {
        UserDetailsDTO userDetailsDTO = UserDetailsDTO.validationRequired(token);
        resolveCallbacks.notifyUserDetailUpdate(userDetailsDTO);
    }

    private boolean resolutionIsOngoing(String location) {
        Pattern pattern = Pattern.compile(".*/users/tokens/[^/]*\\?backendId=.*");
        Matcher matcher = pattern.matcher(location);
        return matcher.matches();
    }

    private void checkStatus() {
        UserDetailsDTO userDetailsDTO = new UserDetailsDTO(ResolutionStatus.STILL_RUNNING);
        worker.sendMessage(worker.createMessage(CHECK_STATUS, userDetailsDTO));
    }

    protected void resolutionFailed() {
        resolveCallbacks.notifyUserDetailUpdate(UserDetailsDTO.FAILED);
    }
}
