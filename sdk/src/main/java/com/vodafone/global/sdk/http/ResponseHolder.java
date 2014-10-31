package com.vodafone.global.sdk.http;

import com.squareup.okhttp.Headers;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class ResponseHolder {

    private final Response response;
    private String body;
    private IOException exception;

    public ResponseHolder(Response response) {
        this.response = response;
    }

    public String body() throws IOException {
        if (body == null) {
            if (exception == null) {
                try {
                    body = response.body().string();
                } catch (IOException e) {
                    exception = e;
                    throw e;
                }
            } else {
                throw exception;
            }
        }
        return body;
    }

    public Protocol protocol() {
        return response.protocol();
    }

    public int code() {
        return response.code();
    }

    public String message() {
        return response.message();
    }

    public Headers headers() {
        return response.headers();
    }

    public String header(String name) {
        return response.header(name);
    }

    public String header(String name, String defaultValue) {
        return response.header(name, defaultValue);
    }
}
