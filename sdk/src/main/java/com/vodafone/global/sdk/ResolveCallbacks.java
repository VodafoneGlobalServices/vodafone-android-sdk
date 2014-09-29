package com.vodafone.global.sdk;

import android.os.Handler;
import android.os.Looper;
import com.vodafone.global.sdk.http.resolve.UserDetailsDTO;
import timber.log.Timber;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class ResolveCallbacks {
    Set<ResolveCallback> resolveCallbacks = new CopyOnWriteArraySet<ResolveCallback>();

    public void add(ResolveCallback callback) {
        resolveCallbacks.add(callback);
    }

    public void remove(ResolveCallback callback) {
        resolveCallbacks.remove(callback);
    }

    public void notifyUserDetailUpdate(final UserDetailsDTO userDetailsDto) {
        Timber.d(userDetailsDto.toString());
        Handler handler = new Handler(Looper.getMainLooper());
        for (final ResolveCallback callback : resolveCallbacks) {
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

    public void notifyError(final VodafoneException exception) {
        Timber.e(exception, exception.getMessage());
        Handler handler = new Handler(Looper.getMainLooper());
        for (final ResolveCallback callback : resolveCallbacks) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onError(exception);
                }
            });
        }
    }

    public void resolutionFailed() {
        notifyUserDetailUpdate(UserDetailsDTO.FAILED);
    }
}
