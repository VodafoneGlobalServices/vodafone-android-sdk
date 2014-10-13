package com.vodafone.global.sdk.logging;

public class LoggerFactory {
    private LoggerFactory() {
    }

    public static Logger getNetworkLogger() {
        return new NullLogger();
    }

    public static Logger getDefaultLogger() {
        return new DefaultLogger();
    }
}
