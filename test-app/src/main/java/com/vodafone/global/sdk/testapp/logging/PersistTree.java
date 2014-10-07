package com.vodafone.global.sdk.testapp.logging;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;
import com.vodafone.global.sdk.testapp.logging.database.LogColumns;
import com.vodafone.global.sdk.testapp.logging.database.LogsProvider;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import timber.log.Timber;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PersistTree implements Timber.TaggedTree {
    private static final Pattern ANONYMOUS_CLASS = Pattern.compile("\\$\\d+$");
    private static final ThreadLocal<String> NEXT_TAG = new ThreadLocal<String>();
    private final Context appContext;

    public PersistTree(Context context) {
        this.appContext = context.getApplicationContext();
    }

    @Override
    public void tag(String tag) {
        NEXT_TAG.set(tag);
    }

    @Override
    public void v(String message, Object... args) {
        throwShade(Log.VERBOSE, formatString(message, args), null);
    }

    @Override
    public void v(Throwable t, String message, Object... args) {
        throwShade(Log.VERBOSE, formatString(message, args), t);
    }

    @Override
    public void d(String message, Object... args) {
        throwShade(Log.DEBUG, formatString(message, args), null);
    }

    @Override
    public void d(Throwable t, String message, Object... args) {
        throwShade(Log.DEBUG, formatString(message, args), t);
    }

    @Override
    public void i(String message, Object... args) {
        throwShade(Log.INFO, formatString(message, args), null);
    }

    @Override
    public void i(Throwable t, String message, Object... args) {
        throwShade(Log.INFO, formatString(message, args), t);
    }

    @Override
    public void w(String message, Object... args) {
        throwShade(Log.WARN, formatString(message, args), null);
    }

    @Override
    public void w(Throwable t, String message, Object... args) {
        throwShade(Log.WARN, formatString(message, args), t);
    }

    @Override
    public void e(String message, Object... args) {
        throwShade(Log.ERROR, formatString(message, args), null);
    }

    @Override
    public void e(Throwable t, String message, Object... args) {
        throwShade(Log.ERROR, formatString(message, args), t);
    }

    static String formatString(String message, Object... args) {
        // If no varargs are supplied, treat it as a request to log the string without formatting.
        return args.length == 0 ? message : String.format(message, args);
    }

    private void throwShade(int priority, String message, Throwable t) {
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

        final ContentValues cv = new ContentValues();
        cv.put(LogColumns.TIMESTAMP, ISODateTimeFormat.dateTime().print(new DateTime()));
        cv.put(LogColumns.LEVEL, priority);
        cv.put(LogColumns.TAG, createTag());
        cv.put(LogColumns.MSG, message);
        new Thread(new Runnable() {
            @Override
            public void run() {
                ContentResolver contentResolver = appContext.getContentResolver();
                contentResolver.insert(LogsProvider.Logs.LOGS, cv);
            }
        }).run();
    }

    private static String createTag() {
        String tag = NEXT_TAG.get();
        if (tag != null) {
            NEXT_TAG.remove();
            return tag;
        }

        StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        if (stackTrace.length < 6) {
            throw new IllegalStateException(
                    "Synthetic stacktrace didn't have enough elements: are you using proguard?");
        }
        tag = stackTrace[5].getClassName();
        Matcher m = ANONYMOUS_CLASS.matcher(tag);
        if (m.find()) {
            tag = m.replaceAll("");
        }
        return tag.substring(tag.lastIndexOf('.') + 1);
    }
}
