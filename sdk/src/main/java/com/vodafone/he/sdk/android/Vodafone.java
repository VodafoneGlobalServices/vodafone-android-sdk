package com.vodafone.he.sdk.android;

import android.app.Application;

/**
 * Use this class to initialize Vodafone SDK and call backend.
 */
public class Vodafone {
    private static Application application;

    /**
     * Initializes SDK for a given Application. The call to this method should
     * be placed as soon as possible in the {@link Application#onCreate()}
     * method.
     * @param app your application class instance
     * @param appId application's identification
     */
    public static void init(Application app, String appId) {
        if (application != null) {
            return; // can't initialize SDK twice
        }

        // TODO SDK initialization
        Vodafone.application = app;
    }

    /**
     * Synchronous call to backend to get user detail.
     * Returns immediately and returns cached object.
     * @return cached object
     */
    public static UserDetails getUserDetails() {
        // TODO return cached object
        return null;
    }

    /**
     * Asynchronous call to backend to get user detail.
     * @param userDetailsCallback callback used to handle success and failure
     * @param options options specific to this call
     */
    public static void getUserDetails(
            UserDetailsCallback userDetailsCallback,
            Options options
    ) {
        // TODO instantiation of backend service if not running
        // TODO add callback to listeners poll
    }
}
