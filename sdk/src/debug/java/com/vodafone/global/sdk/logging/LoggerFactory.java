package com.vodafone.global.sdk.logging;

public class LoggerFactory {
    private LoggerFactory() {
    }

    public static Logger getLogger() {
        return new TimberLogger();
    }
}
