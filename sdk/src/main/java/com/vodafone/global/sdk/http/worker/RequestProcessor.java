package com.vodafone.global.sdk.http.worker;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.google.common.base.Optional;
import com.vodafone.global.sdk.Settings;
import com.vodafone.global.sdk.UserDetailsCallback;
import com.vodafone.global.sdk.VodafoneException;
import com.vodafone.global.sdk.http.oauth.OAuthToken;
import com.vodafone.global.sdk.http.resolve.UserDetailsDTO;

import java.util.Set;

import timber.log.Timber;

public abstract class RequestProcessor {
    protected final Settings settings;
    protected final Context context;
    private final Set<UserDetailsCallback> userDetailsCallbacks;


    public RequestProcessor(Context context, Settings settings, Set<UserDetailsCallback> userDetailsCallback) {
        this.context = context;
        this.settings = settings;
        this.userDetailsCallbacks = userDetailsCallback;
    }

    abstract void process(Worker worker, Optional<OAuthToken> authToken, Message msg);

    protected void notifyUserDetailUpdate(final UserDetailsDTO userDetailsDTO) {
        Timber.d(userDetailsDTO.userDetails.toString());
        Handler handler = new Handler(Looper.getMainLooper());
        for (final UserDetailsCallback callback : userDetailsCallbacks) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onUserDetailsUpdate(userDetailsDTO.userDetails);
                }
            });
        }
    }

    protected void notifyError(final VodafoneException exception) {
        Timber.e(exception, exception.getMessage());
        Handler handler = new Handler(Looper.getMainLooper());
        for (final UserDetailsCallback callback : userDetailsCallbacks) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onUserDetailsError(exception);
                }
            });
        }
    }
}
