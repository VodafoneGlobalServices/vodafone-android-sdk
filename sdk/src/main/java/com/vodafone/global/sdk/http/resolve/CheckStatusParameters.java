package com.vodafone.global.sdk.http.resolve;

import com.google.common.base.Optional;
import com.vodafone.global.sdk.UserDetails;

public class CheckStatusParameters {
    public final Optional<UserDetails> userDetails;
    public final Optional<String> etag;
    public final Optional<Long> retryAfter;

    public CheckStatusParameters() {
        this.userDetails = Optional.absent();
        this.etag = Optional.absent();
        this.retryAfter = Optional.absent();
    }

    public CheckStatusParameters(UserDetails userDetails, String etag, long retryAfter) {
        this.userDetails = Optional.of(userDetails);
        this.etag = Optional.of(etag);
        this.retryAfter = Optional.of(retryAfter);
    }
}
