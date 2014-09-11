package com.vodafone.global.sdk;

/**
 * UserDetailsRequestParameters used to configure call to backend.
 */
public class ValidatePinParameters {
    private final UserDetails userDetails;
    private final String pin;

    private ValidatePinParameters(UserDetails userDetails, String pin) {
        this.userDetails = userDetails;
        this.pin = pin;
    }

    public UserDetails getUserDetails() {
        return userDetails;
    }
    public String getPin() {
        return pin;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class used to initialize UserDetailsRequestParameters.
     */
    public static class Builder {
        private UserDetails userDetails;
        private String pin;

        private Builder() {
        }

        public Builder userDetails(UserDetails userDetails) {
            this.userDetails = userDetails;
            return this;
        }

        public Builder pin(String pin) {
            this.pin = pin;
            return this;
        }

        public ValidatePinParameters build() {
            return new ValidatePinParameters(userDetails, pin);
        }
    }
}
