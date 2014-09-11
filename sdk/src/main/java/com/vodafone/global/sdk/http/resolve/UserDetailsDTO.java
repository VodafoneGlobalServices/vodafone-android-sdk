package com.vodafone.global.sdk.http.resolve;

import com.vodafone.global.sdk.UserDetails;

public class UserDetailsDTO {
    public final UserDetails userDetails;
    public final String etag;
    public final long retryAfter;

    public UserDetailsDTO(UserDetails userDetails, String etag, long retryAfter) {
        this.userDetails = userDetails;
        this.etag = etag;
        this.retryAfter = retryAfter;
    }

    //REMOVE after removal of old resolve queries
    public UserDetailsDTO(UserDetails userDetails, String etag) {
        this.userDetails = userDetails;
        this.etag = etag;
        this.retryAfter = -1;
    }
}
