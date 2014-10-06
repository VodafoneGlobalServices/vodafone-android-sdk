package com.vodafone.global.sdk.testapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Process;
import com.vodafone.global.sdk.Vodafone;
import timber.log.Timber;

public class Application extends android.app.Application {

    static void exit() {
        android.os.Process.killProcess(Process.myPid());
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        SharedPreferences preferences = getSharedPreferences(Preferences.DEFAULT_PREF, Context.MODE_PRIVATE);
        String appKey = preferences.getString(Preferences.APP_KEY, "");
        String appSecret = preferences.getString(Preferences.APP_SECRET, "");
        String backendAppKey = preferences.getString(Preferences.BACKEND_APP_KEY, "");

        Timber.d("initializing; app key: '%s', app secret: '%s', backend key: '%s'",
                appKey, appSecret, backendAppKey);

        Vodafone.init(this, appKey, appSecret, backendAppKey);
    }
}
