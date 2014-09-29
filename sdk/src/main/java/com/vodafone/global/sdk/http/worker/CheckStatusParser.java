package com.vodafone.global.sdk.http.worker;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.squareup.okhttp.Response;
import com.vodafone.global.sdk.GenericServerError;
import com.vodafone.global.sdk.ResolutionCallback;
import com.vodafone.global.sdk.VodafoneException;
import com.vodafone.global.sdk.http.HttpCode;
import com.vodafone.global.sdk.http.parser.Parsers;
import com.vodafone.global.sdk.http.resolve.UserDetailsDTO;
import org.json.JSONException;
import org.json.JSONObject;
import timber.log.Timber;

import java.io.IOException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.Manifest.permission.RECEIVE_SMS;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.vodafone.global.sdk.MessageType.AUTHENTICATE;
import static com.vodafone.global.sdk.MessageType.CHECK_STATUS;
import static com.vodafone.global.sdk.MessageType.GENERATE_PIN;
import static com.vodafone.global.sdk.http.HttpCode.*;
import static com.vodafone.global.sdk.http.HttpCode.NOT_FOUND_404;

public class CheckStatusParser {
    private final Set<ResolutionCallback> resolutionCallbacks;
    private final Context context;
    private final Worker worker;

    public CheckStatusParser(Worker worker, Context context, Set<ResolutionCallback> resolutionCallbacks) {
        this.worker = worker;
        this.resolutionCallbacks = resolutionCallbacks;
        this.context = context;
    }

    void parseResponse(Response response, UserDetailsDTO userDetailsDto) throws IOException, JSONException {
        int code = response.code();
        switch (code) {
            case OK_200:
                notifyUserDetailUpdate(Parsers.resolutionCompleted(response));
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
                worker.sendMessageDelayed(message, redirectDetails.retryAfter);
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
                    notifyError(new GenericServerError());
                }
                break;
            case NOT_FOUND_404:
                resolutionFailed();
                break;
            default:
                notifyError(new GenericServerError());
        }
    }

    protected void notifyUserDetailUpdate(final UserDetailsDTO userDetailsDto) {
        Timber.d(userDetailsDto.toString());
        Handler handler = new Handler(Looper.getMainLooper());
        for (final ResolutionCallback callback : resolutionCallbacks) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    switch (userDetailsDto.status) {
                        case COMPLETED:
                            callback.onCompleted(userDetailsDto.userDetails);
                            break;
                        case STILL_RUNNING:
                            break;
                        case VALIDATION_REQUIRED:
                            callback.onValidationRequired();
                            break;
                        case FAILED:
                            callback.onFailed();
                            break;
                    }
                }
            });
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
        notifyUserDetailUpdate(userDetailsDTO);
    }

    protected void resolutionFailed() {
        notifyUserDetailUpdate(UserDetailsDTO.FAILED);
    }

    protected void notifyError(final VodafoneException exception) {
        Timber.e(exception, exception.getMessage());
        Handler handler = new Handler(Looper.getMainLooper());
        for (final ResolutionCallback callback : resolutionCallbacks) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onError(exception);
                }
            });
        }
    }
}
