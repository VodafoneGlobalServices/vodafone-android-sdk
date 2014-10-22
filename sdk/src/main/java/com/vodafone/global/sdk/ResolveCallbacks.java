package com.vodafone.global.sdk;

import android.os.Handler;
import android.os.Looper;
import com.vodafone.global.sdk.http.resolve.UserDetailsDTO;

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
        Handler handler = new Handler(Looper.getMainLooper());
        for (final ResolveCallback callback : resolveCallbacks) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    switch (userDetailsDto.status) {
                        case COMPLETED:
                            callback.onCompleted(userDetailsDto.userDetails.get());
                            break;
                        case STILL_RUNNING:
                            break;
                        case VALIDATION_REQUIRED:
                            callback.onCompleted(userDetailsDto.userDetails.get()); // TODO temporary solution, before new token caching solution
                            callback.onValidationRequired();
                            break;
                        case UNABLE_TO_RESOLVE:
                            callback.onUnableToResolve();
                            break;
                    }
                }
            });
        }
    }

    public void notifyError(final VodafoneException exception) {
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

    public void unableToResolve() {
        notifyUserDetailUpdate(UserDetailsDTO.UNABLE_TO_RESOLVE);
    }
}
