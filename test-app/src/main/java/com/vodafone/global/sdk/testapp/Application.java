package com.vodafone.global.sdk.testapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Process;
import com.crashlytics.android.Crashlytics;
import com.vodafone.global.sdk.*;
import com.vodafone.global.sdk.testapp.logging.PersistTree;
import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.RECEIVE_SMS;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class Application extends android.app.Application {

    static void exit() {
        android.os.Process.killProcess(Process.myPid());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        Timber.plant(new Timber.DebugTree());
        Timber.plant(new PersistTree(this));

        SharedPreferences preferences = getSharedPreferences(Preferences.DEFAULT_PREF, Context.MODE_PRIVATE);
        String appKey = preferences.getString(Preferences.APP_KEY, Preferences.APP_KEY_DEFAULT);
        String appSecret = preferences.getString(Preferences.APP_SECRET, Preferences.APP_SECRET_DEFAULT);
        String backendAppKey = preferences.getString(Preferences.BACKEND_APP_KEY, Preferences.BACKEND_APP_KEY_DEFAULT);

        Timber.d("initializing; app key: '%s', app secret: '%s', backend key: '%s'",
                appKey, appSecret, backendAppKey);

        Timber.d("can read IMSI: " + (checkCallingOrSelfPermission(READ_PHONE_STATE) == PERMISSION_GRANTED));
        Timber.d("can read SMS: " + (checkCallingOrSelfPermission(RECEIVE_SMS) == PERMISSION_GRANTED));


        Timber.d("sdk build time: %s", com.vodafone.global.sdk.BuildConfig.BUILD_TIME);
        Timber.d("sdk git sha: %s", com.vodafone.global.sdk.BuildConfig.GIT_SHA);
        Timber.d("sdk version name: %s", com.vodafone.global.sdk.BuildConfig.VERSION_NAME);
        Timber.d("sdk version code: %d", com.vodafone.global.sdk.BuildConfig.VERSION_CODE);

        Timber.d("test-app build time: %s", BuildConfig.BUILD_TIME);
        Timber.d("test-app git sha: %s", BuildConfig.GIT_SHA);
        Timber.d("test-app version name: %s", BuildConfig.VERSION_NAME);
        Timber.d("test-app version code: %d", BuildConfig.VERSION_CODE);

        Vodafone.init(this, appKey, appSecret, backendAppKey);
    }
}
