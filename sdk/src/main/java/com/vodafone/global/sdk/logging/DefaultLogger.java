package com.vodafone.global.sdk.logging;

import android.util.Log;

class DefaultLogger implements Logger {
    private static final String TAG = "SeamlessId";

    @Override public void v(String message, Object... args) {
        log(Log.VERBOSE, formatString(message, args), null);
    }

    @Override
    public void v(Throwable throwable) {
        v(throwable, null);
    }

    @Override public void v(Throwable t, String message, Object... args) {
        log(Log.VERBOSE, formatString(message, args), t);
    }

    @Override public void d(String message, Object... args) {
        log(Log.DEBUG, formatString(message, args), null);
    }

    @Override
    public void d(Throwable throwable) {
        d(throwable, null);
    }

    @Override public void d(Throwable t, String message, Object... args) {
        log(Log.DEBUG, formatString(message, args), t);
    }

    @Override public void i(String message, Object... args) {
        log(Log.INFO, formatString(message, args), null);
    }

    @Override
    public void i(Throwable throwable) {
        i(throwable, null);
    }

    @Override public void i(Throwable t, String message, Object... args) {
        log(Log.INFO, formatString(message, args), t);
    }

    @Override public void w(String message, Object... args) {
        log(Log.WARN, formatString(message, args), null);
    }

    @Override
    public void w(Throwable throwable) {
        w(throwable, null);
    }

    @Override public void w(Throwable t, String message, Object... args) {
        log(Log.WARN, formatString(message, args), t);
    }

    @Override public void e(String message, Object... args) {
        log(Log.ERROR, formatString(message, args), null);
    }

    @Override
    public void e(Throwable throwable) {
        e(throwable, null);
    }

    @Override public void e(Throwable t, String message, Object... args) {
        log(Log.ERROR, formatString(message, args), t);
    }

    private static String formatString(String message, Object... args) {
        return args.length == 0 ? message : String.format(message, args);
    }

    private void log(int priority, String message, Throwable t) {
        if (message == null || message.length() == 0) {
            if (t != null) {
                message = Log.getStackTraceString(t);
            } else {
                // Swallow message if it's null and there's no throwable.
                return;
            }
        } else if (t != null) {
            message += "\n" + Log.getStackTraceString(t);
        }

        if (message.length() < 4000) {
            Log.println(priority, TAG, message);
        } else {
            // It's rare that the message will be this large, so we're ok with the perf hit of splitting
            // and calling Log.println N times.  It's possible but unlikely that a single line will be
            // longer than 4000 characters: we're explicitly ignoring this case here.
            String[] lines = message.split("\n");
            for (String line : lines) {
                Log.println(priority, TAG, line);
            }
        }
    }
}
