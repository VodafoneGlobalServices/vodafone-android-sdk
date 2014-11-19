package com.vodafone.global.sdk.testapp;

import android.os.Process;
import com.crashlytics.android.Crashlytics;
import com.vodafone.global.sdk.testapp.logging.PersistTree;
import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.RECEIVE_SMS;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class Application extends android.app.Application {

    static void exit() {
        Process.killProcess(Process.myPid());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        Timber.plant(new Timber.DebugTree());
        Timber.plant(new PersistTree(this));

        Timber.v("can read IMSI: " + (checkCallingOrSelfPermission(READ_PHONE_STATE) == PERMISSION_GRANTED));
        Timber.v("can read SMS: " + (checkCallingOrSelfPermission(RECEIVE_SMS) == PERMISSION_GRANTED));


        Timber.v("sdk build time: %s", com.vodafone.global.sdk.BuildConfig.BUILD_TIME);
        Timber.v("sdk git sha: %s", com.vodafone.global.sdk.BuildConfig.GIT_SHA);
        Timber.v("sdk version name: %s", com.vodafone.global.sdk.BuildConfig.VERSION_NAME);
        Timber.v("sdk version code: %d", com.vodafone.global.sdk.BuildConfig.VERSION_CODE);

        Timber.v("test-app build time: %s", BuildConfig.BUILD_TIME);
        Timber.v("test-app git sha: %s", BuildConfig.GIT_SHA);
        Timber.v("test-app version name: %s", BuildConfig.VERSION_NAME);
        Timber.v("test-app version code: %d", BuildConfig.VERSION_CODE);
    }
}
