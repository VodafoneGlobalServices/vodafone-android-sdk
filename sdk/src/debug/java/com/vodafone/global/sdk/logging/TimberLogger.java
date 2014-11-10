package com.vodafone.global.sdk.logging;

import timber.log.Timber;

public class TimberLogger implements Logger {

    public static final String TAG = "SDK";

    @Override
    public void v(String message, Object... args) {
        Timber.tag(TAG);
        Timber.v(message, args);
    }

    @Override
    public void v(Throwable throwable) {
        Timber.tag(TAG);
        Timber.v(throwable, "");
    }

    @Override
    public void v(Throwable throwable, String message, Object... args) {
        Timber.tag(TAG);
        Timber.v(throwable, message, args);
    }

    @Override
    public void d(String message, Object... args) {
        Timber.tag(TAG);
        Timber.d(message, args);
    }

    @Override
    public void d(Throwable throwable) {
        Timber.tag(TAG);
        Timber.d(throwable, "");
    }

    @Override
    public void d(Throwable throwable, String message, Object... args) {
        Timber.tag(TAG);
        Timber.d(throwable, message, args);
    }

    @Override
    public void i(String message, Object... args) {
        Timber.tag(TAG);
        Timber.d(message, args);
    }

    @Override
    public void i(Throwable throwable) {
        Timber.tag(TAG);
        Timber.i(throwable, "");
    }

    @Override
    public void i(Throwable throwable, String message, Object... args) {
        Timber.tag(TAG);
        Timber.i(throwable, message, args);
    }

    @Override
    public void w(String message, Object... args) {
        Timber.tag(TAG);
        Timber.w(message, args);
    }

    @Override
    public void w(Throwable throwable) {
        Timber.tag(TAG);
        Timber.w(throwable, "");
    }

    @Override
    public void w(Throwable throwable, String message, Object... args) {
        Timber.tag(TAG);
        Timber.w(throwable, message, args);
    }

    @Override
    public void e(String message, Object... args) {
        Timber.tag(TAG);
        Timber.e(message, args);
    }

    @Override
    public void e(Throwable throwable) {
        Timber.tag(TAG);
        Timber.e(throwable, "");
    }

    @Override
    public void e(Throwable throwable, String message, Object... args) {
        Timber.tag(TAG);
        Timber.e(throwable, message, args);
    }
}
