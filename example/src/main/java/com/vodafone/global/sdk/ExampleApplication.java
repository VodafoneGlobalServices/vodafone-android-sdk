package com.vodafone.global.sdk;

import android.app.Application;
import com.vodafone.he.sdk.android.Vodafone;

import static com.vodafone.global.sdk.ExampleConstants.APPLICATION_ID;

public class ExampleApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Vodafone.init(this, APPLICATION_ID);
    }
}
