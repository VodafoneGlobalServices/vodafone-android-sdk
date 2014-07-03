package com.vodafone.he.sdk.android;

public class Options {
    private final boolean smsValidation;
    private final boolean secure;
    private final String secureMessage;

    private Options(boolean smsValidation, boolean secure, String secureMessage) {
        this.smsValidation = smsValidation;
        this.secure = secure;
        this.secureMessage = secureMessage;
    }

    public boolean isSmsValidation() {
        return smsValidation;
    }

    public boolean isSecure() {
        return secure;
    }

    public String getSecureMessage() {
        return secureMessage;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private boolean smsValidation = false;
        private boolean secure = false;
        private String secureMessage = "";

        private Builder() {
            throw new UnsupportedOperationException();
        }

        public Builder enableSmsValidation() {
            smsValidation = true;
            return this;
        }

        public Builder enableSecureFlow() {
            secure = true;
            return this;
        }

        public Builder setSecureMessage(String message) {
            this.secureMessage = message;
            return this;
        }

        public Options build() {
           return new Options(smsValidation, secure, secureMessage);
        }
    }
}
