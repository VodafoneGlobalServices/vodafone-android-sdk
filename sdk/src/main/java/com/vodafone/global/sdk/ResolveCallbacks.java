package com.vodafone.global.sdk;

import android.os.Handler;
import android.os.Looper;
import com.google.common.base.Optional;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class ResolveCallbacks {

    private Optional<String> sessionToken = Optional.absent();

    Set<ResolveCallback> resolveCallbacks = new CopyOnWriteArraySet<ResolveCallback>();

    public void add(ResolveCallback callback) {
        resolveCallbacks.add(callback);
    }

    public void remove(ResolveCallback callback) {
        resolveCallbacks.remove(callback);
    }

    public void completed(final UserDetails userDetails) {
        clearSessionToken();

        Handler handler = new Handler(Looper.getMainLooper());
        for (final ResolveCallback callback : resolveCallbacks) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onCompleted(userDetails);
                }
            });
        }
    }

    public void validationRequired(String token) {
        setSessionToken(token);

        Handler handler = new Handler(Looper.getMainLooper());
        for (final ResolveCallback callback : resolveCallbacks) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onValidationRequired();
                }
            });
        }
    }

    public void notifyError(final VodafoneException exception) {
        clearSessionToken();

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
        clearSessionToken();
        Handler handler = new Handler(Looper.getMainLooper());
        for (final ResolveCallback callback : resolveCallbacks) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onUnableToResolve();
                }
            });
        }
    }

    private void setSessionToken(String token) {
        sessionToken = Optional.of(token);
    }

    private void clearSessionToken() {
        sessionToken = Optional.absent();
    }

    public Optional<String> getSessionToken() {
        return sessionToken;
    }
}
