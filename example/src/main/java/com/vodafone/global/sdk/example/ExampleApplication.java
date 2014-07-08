package com.vodafone.global.sdk.example;

import android.app.Application;
import com.vodafone.global.sdk.Vodafone;

import static com.vodafone.global.sdk.example.ExampleConstants.APPLICATION_ID;

public class ExampleApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Vodafone.init(this, APPLICATION_ID);
    }
}
