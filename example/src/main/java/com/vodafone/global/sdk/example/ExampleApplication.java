package com.vodafone.global.sdk.example;

import android.app.Application;

import com.vodafone.global.sdk.Vodafone;

import timber.log.Timber;

public class ExampleApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Vodafone.init(this, "I1OpZaPfBcI378Bt7PBhQySW5Setb8eb",
                "k4l1RXZGqMnw2cD8", "1234");
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}
