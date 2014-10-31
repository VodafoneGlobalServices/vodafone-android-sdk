package com.vodafone.global.sdk.logging;

import timber.log.Timber;

public class TimberLogger implements Logger {
    @Override
    public void v(String message, Object... args) {
        Timber.v(message, args);
    }

    @Override
    public void v(Throwable throwable) {
        Timber.v(throwable, "");
    }

    @Override
    public void v(Throwable throwable, String message, Object... args) {
        Timber.v(throwable, message, args);
    }

    @Override
    public void d(String message, Object... args) {
        Timber.d(message, args);
    }

    @Override
    public void d(Throwable throwable) {
        Timber.d(throwable, "");
    }

    @Override
    public void d(Throwable throwable, String message, Object... args) {
        Timber.d(throwable, message, args);
    }

    @Override
    public void i(String message, Object... args) {
        Timber.d(message, args);
    }

    @Override
    public void i(Throwable throwable) {
        Timber.i(throwable, "");
    }

    @Override
    public void i(Throwable throwable, String message, Object... args) {
        Timber.i(throwable, message, args);
    }

    @Override
    public void w(String message, Object... args) {
        Timber.w(message, args);
    }

    @Override
    public void w(Throwable throwable) {
        Timber.w(throwable, "");
    }

    @Override
    public void w(Throwable throwable, String message, Object... args) {
        Timber.w(throwable, message, args);
    }

    @Override
    public void e(String message, Object... args) {
        Timber.e(message, args);
    }

    @Override
    public void e(Throwable throwable) {
        Timber.e(throwable, "");
    }

    @Override
    public void e(Throwable throwable, String message, Object... args) {
        Timber.e(throwable, message, args);
    }
}
