package com.vodafone.global.sdk.logging;

public interface Logger {
    void v(String message, Object... args);
    void v(Throwable throwable);
    void v(Throwable throwable, String message, Object... args);

    void d(String message, Object... args);
    void d(Throwable throwable);
    void d(Throwable throwable, String message, Object... args);

    void i(String message, Object... args);
    void i(Throwable throwable);
    void i(Throwable throwable, String message, Object... args);

    void w(String message, Object... args);
    void w(Throwable throwable);
    void w(Throwable throwable, String message, Object... args);

    void e(String message, Object... args);
    void e(Throwable throwable);
    void e(Throwable throwable, String message, Object... args);
}
