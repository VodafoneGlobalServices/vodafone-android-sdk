package com.vodafone.global.sdk;

public class MSISDN {

    private final String msisdn;

    public MSISDN(String msisdn) {
        this.msisdn = msisdn;
    }

    public boolean isPresent() {
        return msisdn == null || msisdn.isEmpty();
    }

    public boolean isValid() {
        // TODO msisdn regex matching against configuration value
        // (msisdn.matches(settings.msisdnValidationRegex) || msisdn.isEmpty())
        return true;
    }

    public String marketCode() {
        // TODO
        return "DE";
    }

    public String get() {
        return msisdn;
    }
}
