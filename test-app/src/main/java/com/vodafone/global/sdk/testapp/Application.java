package com.vodafone.global.sdk.testapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Process;
import com.vodafone.global.sdk.Vodafone;

public class Application extends android.app.Application {

    static void exit() {
        android.os.Process.killProcess(Process.myPid());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences preferences = getSharedPreferences(Preferences.DEFAULT_PREF, Context.MODE_PRIVATE);
        String appId = preferences.getString(Preferences.APP_ID, "");
        Vodafone.init(this, appId);
    }
}
