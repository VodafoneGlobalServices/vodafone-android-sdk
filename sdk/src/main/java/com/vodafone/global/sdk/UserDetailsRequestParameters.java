package com.vodafone.global.sdk;

/**
 * UserDetailsRequestParameters used to configure call to backend.
 */
public class UserDetailsRequestParameters {
    private final boolean smsValidation;
    private final String msisdn;

    private UserDetailsRequestParameters(boolean smsValidation, String msisdn) {
        this.smsValidation = smsValidation;
        this.msisdn = msisdn;
    }

    public boolean smsValidation() {
        return smsValidation;
    }

    public String getMSISDN() {
        return msisdn;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class used to initialize UserDetailsRequestParameters.
     */
    public static class Builder {
        private boolean smsValidation = false;
        private String msisdn = "";

        private Builder() {
        }

        public Builder enableSmsValidation() {
            smsValidation = true;
            return this;
        }

        public Builder msisdn(String value) {
            msisdn = value;
            return this;
        }

        public Builder setSmsValidation(boolean value) {
            smsValidation = value;
            return this;
        }

        public UserDetailsRequestParameters build() {
            return new UserDetailsRequestParameters(smsValidation, msisdn);
        }
    }
}
