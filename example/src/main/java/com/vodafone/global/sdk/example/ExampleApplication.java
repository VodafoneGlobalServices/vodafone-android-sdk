package com.vodafone.global.sdk.example;

import android.app.Application;
import com.vodafone.global.sdk.Vodafone;

import static com.vodafone.global.sdk.example.ExampleConstants.APPLICATION_ID;

public class ExampleApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Vodafone.init(this, "I1OpZaPfBcI378Bt7PBhQySW5Setb8eb",
                "k4l1RXZGqMnw2cD8", "1234");
    }
}
