package com.vodafone.he.sdk.android;

import android.app.Application;

public class Vodafone {
    private static Application application;

    public static void init(Application app, String appId) {
        if (application != null) {
            return; // can't initialize SDK twice
        }

        // TODO SDK initialization
        Vodafone.application = app;
    }

    public static UserDetails getUserDetails() {
        // TODO return cached object
        return null;
    }

    public static void getUserDetails(
            UserDetailsCallback userDetailsCallback,
            Options options
    ) {
        // TODO instantiation of backend service if not running
    }
}
