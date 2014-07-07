package com.vodafone.he.sdk.android;

import android.app.Application;

import java.lang.IllegalArgumentException;

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
     * Retrieves UserDetails from cache.
     * Returns immediately and returns cached object.
     * @return cached object
     */
    public static UserDetails getUserDetails() {
        // TODO return cached object
        return null;
    }

    /**
     * Asynchronous call to backend to get user detail.
     * @param parameters parameters specific to this call
     */
    public static void retrieveUserDetails(UserDetailsRequestParameters parameters) {
        // TODO instantiation of backend service if not running
    }

    /**
     * Used to register callbacks.
     * @throws IllegalArgumentException if callback is of unknown type
     */
    public static void register(VodafoneCallback callback) {
        // TODO need to be refactored so that we don't need to add another `if` each time we want to add new callback
        if (implementsInterface(callback, UserDetailsCallback.class)) {
            // TODO register UserDetailsCallback
        }
        if (implementsInterface(callback, ValidateSmsCallback.class)) {
            // TODO register ValidateSmsCallback
        }
    }

    private static boolean implementsInterface(VodafoneCallback callback, Class callbackType) {
        for (Class c : callback.getClass().getInterfaces())
            if (c.equals(callbackType))
                return true;
        return false;
    }

    /**
     * Used to unregister callback.
     *
     * @param callback callback to be unregistered
     * @throws IllegalArgumentException if callback is of unknown type
     */
    public static void unregister(VodafoneCallback callback) {
        // TODO unregister callback
        // TODO implementation similar to #register(VodafoneCallback)
    }

    /**
     * Validates identity by providing code send by server via SMS.
     *
     * @param code code send to user via SMS
     */
    public static void validateSmsCode(String code) {
        // TODO make a request to APIX
    }
}
