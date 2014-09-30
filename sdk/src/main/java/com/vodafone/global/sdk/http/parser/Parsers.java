package com.vodafone.global.sdk.http.parser;

import com.squareup.okhttp.Response;
import com.vodafone.global.sdk.ResolutionStatus;
import com.vodafone.global.sdk.UserDetails;
import com.vodafone.global.sdk.http.resolve.UserDetailsDTO;
import org.json.JSONException;

import java.io.IOException;

public class Parsers {

    public static UserDetailsDTO resolutionCompleted(Response response) throws JSONException, IOException {
        String jsonString = response.body().string();
        UserDetails userDetails = UserDetails.fromJson(jsonString);
        return new UserDetailsDTO(ResolutionStatus.COMPLETED, userDetails);
    }

    public static UserDetailsDTO parseUserDetails(Response response) throws IOException, JSONException {
        String jsonString = response.body().string();
        UserDetails userDetails = UserDetails.fromJson(jsonString);
        String etag = response.header("etag");
        return new UserDetailsDTO(ResolutionStatus.FIXME, userDetails, etag, -1);
    }

    public static UserDetailsDTO updateRetryAfter(UserDetailsDTO userDetailsDTO, Response response) {
        int retryAfter = Integer.valueOf(response.header("RetryAfter", "500"));
        return new UserDetailsDTO(ResolutionStatus.FIXME, userDetailsDTO.userDetails.get(), userDetailsDTO.etag.get(), retryAfter);
    }
}
