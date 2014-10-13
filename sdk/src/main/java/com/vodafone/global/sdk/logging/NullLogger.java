package com.vodafone.global.sdk.logging;

/**
 * This implementation should be used in production when we want to turn logging off.
 */
class NullLogger implements Logger {
    @Override public void v(String message, Object... args) {}
    @Override public void v(Throwable throwable) {}
    @Override public void v(Throwable throwable, String message, Object... args) {}
    @Override public void d(String message, Object... args) {}
    @Override public void d(Throwable throwable) {}
    @Override public void d(Throwable throwable, String message, Object... args) {}
    @Override public void i(String message, Object... args) {}
    @Override public void i(Throwable throwable) {}
    @Override public void i(Throwable throwable, String message, Object... args) {}
    @Override public void w(String message, Object... args) {}
    @Override public void w(Throwable throwable) {}
    @Override public void w(Throwable throwable, String message, Object... args) {}
    @Override public void e(String message, Object... args) {}
    @Override public void e(Throwable throwable) {}
    @Override public void e(Throwable throwable, String message, Object... args) {}
}
