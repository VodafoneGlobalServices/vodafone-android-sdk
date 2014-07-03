package com.vodafone.he.sdk.android.example;

import android.app.Application;
import com.vodafone.he.sdk.android.Vodafone;

import static com.vodafone.he.sdk.android.example.ExampleConstants.APPLICATION_ID;

public class ExampleApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Vodafone.init(APPLICATION_ID);
    }
}
