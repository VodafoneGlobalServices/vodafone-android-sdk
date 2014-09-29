package com.vodafone.global.sdk.http.worker;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.google.common.base.Optional;
import com.vodafone.global.sdk.ResolutionCallback;
import com.vodafone.global.sdk.Settings;
import com.vodafone.global.sdk.VodafoneException;
import com.vodafone.global.sdk.http.oauth.OAuthToken;
import com.vodafone.global.sdk.http.resolve.UserDetailsDTO;
import timber.log.Timber;

import java.util.Set;

public abstract class RequestProcessor {
    protected final Worker worker;
    protected final Settings settings;
    protected final Context context;
    protected final Set<ResolutionCallback> resolutionCallbacks;

    public RequestProcessor(Context context, Worker worker, Settings settings, Set<ResolutionCallback> resolutionCallback) {
        this.context = context;
        this.worker = worker;
        this.settings = settings;
        this.resolutionCallbacks = resolutionCallback;
    }

    abstract void process(Optional<OAuthToken> authToken, Message msg);

    /**
     * @deprecated need's to move to parser
     */
    @Deprecated
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

    /**
     * @deprecated moved to parser
     */
    @Deprecated
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

    /**
     * @deprecated moved to parser
     */
    @Deprecated
    protected void resolutionFailed() {
        notifyUserDetailUpdate(UserDetailsDTO.FAILED);
    }
}
