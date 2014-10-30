package com.vodafone.global.sdk.http.resolve;

import com.google.common.base.Optional;

public class CheckStatusParameters {
    public final Optional<String> tokenId;
    public final Optional<String> etag;
    public final Optional<Long> retryAfter;

    public CheckStatusParameters(String tokenId) {
        this.tokenId = Optional.of(tokenId);
        this.etag = Optional.absent();
        this.retryAfter = Optional.absent();
    }

    public CheckStatusParameters(String tokenId, String etag, long retryAfter) {
        this.tokenId = Optional.of(tokenId);
        this.etag = Optional.of(etag);
        this.retryAfter = Optional.of(retryAfter);
    }
}
