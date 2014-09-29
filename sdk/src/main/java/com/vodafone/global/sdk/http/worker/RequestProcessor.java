package com.vodafone.global.sdk.http.worker;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.google.common.base.Optional;
import com.vodafone.global.sdk.*;
import com.vodafone.global.sdk.http.oauth.OAuthToken;
import com.vodafone.global.sdk.http.resolve.UserDetailsDTO;
import timber.log.Timber;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.Manifest.permission.RECEIVE_SMS;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.vodafone.global.sdk.MessageType.GENERATE_PIN;

public abstract class RequestProcessor {
    protected final Worker worker;
    protected final Settings settings;
    protected final Context context;
    private final Set<ResolutionCallback> resolutionCallbacks;

    public RequestProcessor(Context context, Worker worker, Settings settings, Set<ResolutionCallback> resolutionCallback) {
        this.context = context;
        this.worker = worker;
        this.settings = settings;
        this.resolutionCallbacks = resolutionCallback;
    }

    abstract void process(Optional<OAuthToken> authToken, Message msg);

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

    protected boolean requiresSmsValidation(String location) {
        Pattern pattern = Pattern.compile(".*/users/tokens/[^/]*/pins\\?backendId=.*");
        Matcher matcher = pattern.matcher(location);
        return matcher.matches();
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

    protected void generatePin(String token) {
        worker.sendMessage(worker.createMessage(GENERATE_PIN, token));
    }

    protected boolean canReadSMS() {
        return context.checkCallingOrSelfPermission(RECEIVE_SMS) == PERMISSION_GRANTED;
    }

    protected void resolutionFailed() {
        notifyUserDetailUpdate(UserDetailsDTO.FAILED);
    }
}
