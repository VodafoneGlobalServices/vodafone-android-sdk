package com.vodafone.global.sdk.http.resolve;

import com.vodafone.global.sdk.UserDetails;

public class UserDetailsDTO {
    public final UserDetails userDetails;
    public final String etag;

    public UserDetailsDTO(UserDetails userDetails, String etag) {
        this.userDetails = userDetails;
        this.etag = etag;
    }
}
