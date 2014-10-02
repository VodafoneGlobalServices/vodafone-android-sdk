package com.vodafone.global.sdk.example;

import android.app.Application;

import com.vodafone.global.sdk.Vodafone;

import timber.log.Timber;

public class ExampleApplication extends Application {

    public static final String APP_KEY = "I1OpZaPfBcI378Bt7PBhQySW5Setb8eb";
    public static final String APP_SECRET = "k4l1RXZGqMnw2cD8";
    public static final String BACKEND_APP_KEY = "6V8HQ9JCSeRBGDhLGRApx9GBaXqTKeuY";

    @Override
    public void onCreate() {
        super.onCreate();

        Vodafone.init(this, APP_KEY, APP_SECRET, BACKEND_APP_KEY);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}
