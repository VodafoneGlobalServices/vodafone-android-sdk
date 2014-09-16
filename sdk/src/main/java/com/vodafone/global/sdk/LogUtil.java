package com.vodafone.global.sdk;

import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

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

        String[] headers = request.headers().toString().split("\\n");
        Arrays.sort(headers);
        for (String header : headers) {
            b.append(":: ").append(header).append("\n");
        }

        Buffer buffer = new Buffer();
        try {
            request.body().writeTo(buffer);
            b.append("\n").append(buffer.readUtf8());
        } catch (IOException e) {
            b.append("\nException while reading body: ").append(e.getMessage());
        }

        Timber.d(b.toString());
    }

    public static void log(Response response) {
        StringBuilder b = new StringBuilder("Response:\n");

        b.append(response.protocol().toString()).append(" ")
                .append(response.code()).append(" ")
                .append(response.message()).append("\n");

        String[] headers = response.headers().toString().split("\\n");
        Arrays.sort(headers);
        for (String header : headers) {
            b.append(":: ").append(header).append("\n");
        }

        try {
            b.append(response.body().string());
        } catch (IOException e) {
            b.append("\nException while reading body: ").append(e.getMessage());
        }

        Timber.d(b.toString());
    }
}
