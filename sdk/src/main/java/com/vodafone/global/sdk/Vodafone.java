package com.vodafone.global.sdk;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;

import static android.provider.Settings.Secure;

/**
 * Use this class to initialize Vodafone SDK and call backend.
 *
 */
public class Vodafone {
    private static Application application;
    private static VodafoneManager manager;

    /**
     * Initializes SDK for a given Application. The call to this method should
     * be placed as soon as possible in the {@link Application#onCreate()}
     * method.
     *
     * @param app   your application class instance
     * @param appId application's identification
     */
    public static void init(Application app, String appId) {
        if (application != null) {
            return; // can't initialize SDK twice
        }

        Vodafone.application = app;
        manager = new VodafoneManager(app, appId);
    }

    /**
     * Retrieves ANDROID_ID from system.
     * @see android.provider.Settings.Secure#ANDROID_ID
     */
    private String getAndroidId(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        return Secure.getString(contentResolver, Secure.ANDROID_ID);
    }

    /**
     * Retrieves UserDetails from cache.
     * Returns immediately and returns cached object.
     *
     * @return cached object
     */
    public static UserDetails getUserDetails() {
        return manager.getUserDetails();
    }

    /**
     * Asynchronous call to backend to get user detail.
     *
     * @param parameters parameters specific to this call
     */
    public static void retrieveUserDetails(UserDetailsRequestParameters parameters) {
        manager.retrieveUserDetails(parameters);
    }

    /**
     * Used to register callbacks.
     *
     * @throws IllegalArgumentException if callback is of unknown type
     */
    public static void register(VodafoneCallback callback) {
        manager.register(callback);
    }

    /**
     * Validates identity by providing code send by server via SMS.
     *
     * @param code code send to user via SMS
     */
    public static void validateSmsCode(String code) {
        manager.validateSmsCode(code);
    }

    /**
     * Used to unregister callback.
     *
     * @param callback callback to be unregistered
     * @throws IllegalArgumentException if callback is of unknown type
     */
    public static void unregister(VodafoneCallback callback) {
        manager.unregister(callback);
    }
}
