package com.vodafone.global.sdk.http.resolve;

import com.google.common.base.Optional;
import com.vodafone.global.sdk.ResolutionStatus;
import com.vodafone.global.sdk.UserDetails;

public class UserDetailsDTO {
    public static final UserDetailsDTO UNABLE_TO_RESOLVE = new UserDetailsDTO(ResolutionStatus.UNABLE_TO_RESOLVE);
    public final ResolutionStatus status;
    public final Optional<UserDetails> userDetails;
    public final Optional<String> etag;
    public final Optional<Long> retryAfter;

    public UserDetailsDTO(ResolutionStatus status) {
        this.status = status;
        this.userDetails = Optional.absent();
        this.etag = Optional.absent();
        this.retryAfter = Optional.absent();
    }

    public UserDetailsDTO(ResolutionStatus status, UserDetails userDetails) {
        this.status = status;
        this.userDetails = Optional.of(userDetails);
        this.etag = Optional.absent();
        this.retryAfter = Optional.absent();
    }

    public UserDetailsDTO(ResolutionStatus status, UserDetails userDetails, String etag, long retryAfter) {
        this.status = status;
        this.userDetails = Optional.of(userDetails);
        this.etag = Optional.of(etag);
        this.retryAfter = Optional.of(retryAfter);
    }

    public static UserDetailsDTO validationRequired(String token) {
        UserDetails userDetails = UserDetails.validationRequired(token);
        return new UserDetailsDTO(ResolutionStatus.VALIDATION_REQUIRED, userDetails);
    }
}
