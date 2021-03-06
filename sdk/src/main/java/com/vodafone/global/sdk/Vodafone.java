package com.vodafone.global.sdk;

import android.app.Application;
import android.content.Context;
import com.vodafone.global.sdk.logging.LoggerFactory;

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
     *yet
     * @param app   your application class instance
     * @param appKey application's identification to obtain access token
     * @param appSecret application's secret to obtain access token
     * @param backendAppKey backend identification key to identify app
     */
    public static void init(Application app, String appKey, String appSecret, String backendAppKey) {
        if (application != null) {
            LoggerFactory.getLogger().w("SDK is already initialized, ignoring init");
            return; // can't initialize SDK twice
        }

        LoggerFactory.getLogger().d("initialized SDK with:" +
                        "\napp key: '%s'," +
                        "\napp secret: '%s'," +
                        "\nbackend key: '%s'",
                appKey, appSecret, backendAppKey);

        Vodafone.application = app;
        manager = new VodafoneManager(app, appKey, appSecret, backendAppKey);
    }

    /**
     * Asynchronous call to backend to get user detail.
     *
     * @param parameters parameters specific to this call
     */
    public static void retrieveUserDetails(UserDetailsRequestParameters parameters) {
        if (manager == null) {
            throw new NotInitialized();
        }
        manager.retrieveUserDetails(parameters);
    }

    /**
     * Used to register callbacks.
     *
     * @throws IllegalArgumentException if callback is of unknown type
     */
    public static void register(VodafoneCallback callback) {
        if (manager == null) {
            throw new NotInitialized();
        }
        manager.register(callback);
    }

    /**
     * Used to generate validation PIN
     */
    public static void generatePin() {
        if (manager == null) {
            throw new NotInitialized();
        }
        manager.generatePin();
    }

    /**
     * Validates identity by providing code send by server via SMS.
     *
     * @param code code send to user via SMS
     */
    public static void validateSmsCode(String code) {
        if (manager == null) {
            throw new NotInitialized();
        }
        manager.validateSmsCode(code);
    }

    /**
     * Used to unregister callback.
     *
     * @param callback callback to be unregistered
     * @throws IllegalArgumentException if callback is of unknown type
     */
    public static void unregister(VodafoneCallback callback) {
        if (manager == null) {
            throw new NotInitialized();
        }
        manager.unregister(callback);
    }

    public static Context getAppContext() {
        return application;
    }
}
