package com.vodafone.global.sdk.logging;

import com.squareup.okhttp.Headers;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.vodafone.global.sdk.http.ResponseHolder;
import okio.Buffer;

import java.io.IOException;
import java.util.Arrays;

public class LogUtil {
    private LogUtil() {
    }

    public static String prepareRequestLogMsg(Request request) {
        StringBuilder builder = new StringBuilder();
        addTitle(request, builder);
        addHeaders(request.headers(), builder);
        addRequestBody(request, builder);
        return builder.toString();
    }

    public static String prepareResponseLogMsg(ResponseHolder response) {
        StringBuilder b = new StringBuilder();
        addTitle(response, b);
        addHeaders(response.headers(), b);
        addResponseBody(response, b);
        return b.toString();
    }

    private static void addTitle(Request request, StringBuilder builder) {
        String method = request.method();
        String url = request.urlString();
        String title = String.format("Request:\n%s %s", method, url);
        builder.append(title);
    }

    private static void addTitle(ResponseHolder response, StringBuilder builder) {
        String protocol = response.protocol().toString();
        int code = response.code();
        String message = response.message();
        String title = String.format("Response:\n%s %d %s", protocol, code, message);
        builder.append(title);
    }

    private static void addHeaders(Headers headers, StringBuilder builder) {
        String[] values = headers.toString().split("\\n");
        Arrays.sort(values);
        for (String header : values) {
            String line = String.format("\n:: %s", header);
            builder.append(line);
        }
    }

    private static void addRequestBody(Request request, StringBuilder builder) {
        try {
            Buffer buffer = new Buffer();
            RequestBody body = request.body();
            if (body != null) {
                body.writeTo(buffer);
                builder.append(String.format("\nbodyStart>%s<bodyEnd", buffer.readUtf8()));
            } else {
                builder.append("\nbodyStart><bodyEnd");
            }
        } catch (IOException e) {
            addExceptionMsg(e, builder);
        }
    }

    private static void addResponseBody(ResponseHolder response, StringBuilder builder) {
        try {
            String body = String.format("\nbodyStart>%s<bodyEnd", response.body());
            builder.append(body);
        } catch (IOException e) {
            addExceptionMsg(e, builder);
        }
    }

    private static void addExceptionMsg(IOException exception, StringBuilder builder) {
        String line = String.format("\nException while reading body: %s", exception.getMessage());
        builder.append(line);
    }
}
