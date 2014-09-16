package com.vodafone.global.sdk;

import com.squareup.okhttp.Request;

import java.io.IOException;
import java.util.Arrays;

import okio.Buffer;
import timber.log.Timber;

public class LogUtil {
    private LogUtil() {
    }

    public static void log(Request request) {
        StringBuilder b = new StringBuilder("Request:\n");
        b.append(request.method()).append(" ").append(request.urlString()).append('\n');

        String[] split = request.headers().toString().split("\\n");
        Arrays.sort(split);
        for (String header : split) {
            b.append(":: ").append(header).append("\n");
        }

        Buffer buffer = new Buffer();
        try {
            request.body().writeTo(buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        b.append("\n").append(buffer.readUtf8());

        Timber.d(b.toString());
    }
}
