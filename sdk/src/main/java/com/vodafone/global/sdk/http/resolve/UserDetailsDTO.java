package com.vodafone.global.sdk.http.resolve;

import com.vodafone.global.sdk.ResolutionStatus;
import com.vodafone.global.sdk.UserDetails;

public class UserDetailsDTO {
    public static final UserDetailsDTO FAILED = new UserDetailsDTO(ResolutionStatus.FAILED);
    public final ResolutionStatus status;
    public final UserDetails userDetails; // FIXME might be absent
    public final String etag; // FIXME might be absent
    public final long retryAfter; // FIXME might be absent

    // TODO fix default values to Optionals

    public UserDetailsDTO(ResolutionStatus status) {
        this.status = status;
        this.userDetails = null;
        this.etag = null;
        this.retryAfter = 0;
    }

    public UserDetailsDTO(ResolutionStatus status, UserDetails userDetails) {
        this.status = status;
        this.userDetails = userDetails;
        this.etag = null;
        this.retryAfter = 0;
    }

    public UserDetailsDTO(ResolutionStatus status, UserDetails userDetails, String etag, long retryAfter) {
        this.status = status;
        this.userDetails = userDetails;
        this.etag = etag;
        this.retryAfter = retryAfter;
    }

    //REMOVE after removal of old resolve queries
    public UserDetailsDTO(ResolutionStatus status, UserDetails userDetails, String etag) {
        this.status = status;
        this.userDetails = userDetails;
        this.etag = etag;
        this.retryAfter = -1;
    }

    public static UserDetailsDTO validationRequired(String token) {
        UserDetails userDetails = UserDetails.validationRequired(token);
        return new UserDetailsDTO(ResolutionStatus.VALIDATION_REQUIRED, userDetails);
    }
}
