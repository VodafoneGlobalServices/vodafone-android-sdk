package com.vodafone.global.sdk.http.parser;

import com.squareup.okhttp.Response;
import com.vodafone.global.sdk.UserDetails;
import com.vodafone.global.sdk.http.resolve.UserDetailsDTO;

import org.json.JSONException;

import java.io.IOException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parsers {
    public static UserDetailsDTO parseUserDetails(Response response) throws IOException, JSONException {
        String jsonString = response.body().string();
        UserDetails userDetails = UserDetails.fromJson(jsonString);
        String etag = response.header("etag");
        return new UserDetailsDTO(userDetails, etag, -1);
    }

    public static UserDetailsDTO parseRedirectDetails(Response response) {
        String location = response.header("Location", "");
        String etag = response.header("etag");

        int retryAfter = Integer.valueOf(response.header("RetryAfter", "-1"));
        boolean pins = location.toLowerCase().contains("pins");

        Pattern pattern = Pattern.compile("tokens/([a-zA-Z_0-9]+)[\\?|/]");
        Matcher matcher = pattern.matcher(location);
        String token = matcher.find() ? matcher.group(1) : "";

        UserDetails userDetails = UserDetails.builder()
                                    .resolved(false)
                                    .stillRunning(retryAfter >= 0 ? true : false)
                                    .token(token)
                                    .expires(new Date(0))
                                    .validationRequired(pins)
                                    .acr("").build();
        return new UserDetailsDTO(userDetails, etag, retryAfter);
    }

    public static UserDetailsDTO updateRetryAfter(UserDetailsDTO oldDetails, Response response) {
        int retryAfter = Integer.valueOf(response.header("RetryAfter", "-1"));
        return new UserDetailsDTO(oldDetails.userDetails, oldDetails.etag, retryAfter);
    }
}
