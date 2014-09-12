package com.vodafone.global.sdk;

/**
 * UserDetailsRequestParameters used to configure call to backend.
 */
public class ValidatePinParameters {
    private final String token;
    private final String pin;

    private ValidatePinParameters(String token, String pin) {
        this.token = token;
        this.pin = pin;
    }

    public String getToken() {
        return token;
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
        private String token;
        private String pin;

        private Builder() {
        }

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder pin(String pin) {
            this.pin = pin;
            return this;
        }

        public ValidatePinParameters build() {
            return new ValidatePinParameters(token, pin);
        }
    }
}
