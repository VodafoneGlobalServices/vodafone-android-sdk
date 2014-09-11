package com.vodafone.global.sdk;

/**
 * UserDetailsRequestParameters used to configure call to backend.
 */
public class UserDetailsRequestParameters {
    private final boolean smsValidation;
    private final String MSISDN;

    private UserDetailsRequestParameters(boolean smsValidation, String MSISDN) {
        this.smsValidation = smsValidation;
        this.MSISDN = MSISDN;
    }

    public boolean smsValidation() {
        return smsValidation;
    }
    public String getMSISDN() {
        return MSISDN;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class used to initialize UserDetailsRequestParameters.
     */
    public static class Builder {
        private boolean smsValidation = false;
        private String MSISDN;

        private Builder() {
        }

        public Builder enableSmsValidation() {
            smsValidation = true;
            return this;
        }

        public Builder setMSISDN(String value) {
            MSISDN = value;
            return this;
        }

        public Builder setSmsValidation(boolean value) {
            smsValidation = value;
            return this;
        }

        public UserDetailsRequestParameters build() {
            return new UserDetailsRequestParameters(smsValidation, MSISDN);
        }
    }
}
