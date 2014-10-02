package com.vodafone.global.sdk.http.resolve;

import android.content.Context;
import android.os.Message;
import com.squareup.okhttp.Response;
import com.vodafone.global.sdk.GenericServerError;
import com.vodafone.global.sdk.ResolveCallbacks;
import com.vodafone.global.sdk.http.HttpCode;
import com.vodafone.global.sdk.http.parser.Parsers;
import com.vodafone.global.sdk.Worker;
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

    void parseResponse(Response response, UserDetailsDTO userDetailsDto) throws IOException, JSONException {
        int code = response.code();
        switch (code) {
            case OK_200:
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
                } else {
                    int retryAfter = Integer.valueOf(response.header("Retry-After", "500"));
                    Message message = worker.createMessage(CHECK_STATUS, userDetailsDto);
                    worker.sendMessageDelayed(message, retryAfter);
                }
                break;
            case HttpCode.NOT_MODIFIED_304:
                UserDetailsDTO redirectDetails = Parsers.updateRetryAfter(userDetailsDto, response);
                Message message = worker.createMessage(CHECK_STATUS, redirectDetails);
                worker.sendMessageDelayed(message, redirectDetails.retryAfter.get());
                break;
            case BAD_REQUEST_400:
                resolutionFailed();
                break;
            case FORBIDDEN_403:
                String body = response.body().string();
                if (!body.isEmpty()) {
                    JSONObject json = new JSONObject(response.body().string());
                    String id = json.getString("id");
                    if (id.equals("POL0002")) {
                        worker.sendMessage(worker.createMessage(AUTHENTICATE));
                        worker.sendMessage(worker.createMessage(CHECK_STATUS, userDetailsDto));
                    }
                } else {
                    resolveCallbacks.notifyError(new GenericServerError());
                }
                break;
            case NOT_FOUND_404:
                resolutionFailed();
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

    protected void validationRequired(String token) {
        UserDetailsDTO userDetailsDTO = UserDetailsDTO.validationRequired(token);
        resolveCallbacks.notifyUserDetailUpdate(userDetailsDTO);
    }

    protected void resolutionFailed() {
        resolveCallbacks.notifyUserDetailUpdate(UserDetailsDTO.FAILED);
    }
}
