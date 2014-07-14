package com.vodafone.global.sdk;

/**
 * UserDetailsRequestParameters used to configure call to backend.
 */
public class UserDetailsRequestParameters {
    private final boolean smsValidation;

    private UserDetailsRequestParameters(boolean smsValidation) {
        this.smsValidation = smsValidation;
    }

    public boolean isSmsValidation() {
        return smsValidation;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class used to initialize UserDetailsRequestParameters.
     */
    public static class Builder {
        private boolean smsValidation = false;

        private Builder() {
            throw new UnsupportedOperationException();
        }

        public Builder enableSmsValidation() {
            smsValidation = true;
            return this;
        }

        public UserDetailsRequestParameters build() {
            return new UserDetailsRequestParameters(smsValidation);
        }
    }
}
