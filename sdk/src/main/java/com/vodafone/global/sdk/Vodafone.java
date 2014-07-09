package com.vodafone.global.sdk;

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

        Vodafone.application = app;
        registrars = prepareRegistrars();
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
        Sets.SetView<Class<?>> knownAndImplementedCallbacksTypes = getKnownAndImplementedCallbacksTypes(callback);

        if (knownAndImplementedCallbacksTypes.isEmpty())
            throw new IllegalArgumentException("Unknown type of callback");

        for (Class c : knownAndImplementedCallbacksTypes)
            registrars.get(c).register(callback);
    }

    /**
     * Validates identity by providing code send by server via SMS.
     *
     * @param code code send to user via SMS
     */
    public static void validateSmsCode(String code) {
        // TODO make a request to APIX
    }

    /**
     * Used to unregister callback.
     *
     * @param callback callback to be unregistered
     * @throws IllegalArgumentException if callback is of unknown type
     */
    public static void unregister(VodafoneCallback callback) {
        Sets.SetView<Class<?>> knownAndImplementedCallbacksTypes = getKnownAndImplementedCallbacksTypes(callback);

        if (knownAndImplementedCallbacksTypes.isEmpty())
            throw new IllegalArgumentException("Unknown type of callback");

        for (Class c : knownAndImplementedCallbacksTypes)
            registrars.get(c).unregister(callback);
    }

    private static Sets.SetView<Class<?>> getKnownAndImplementedCallbacksTypes(VodafoneCallback callback) {
        Set<Class<?>> knownCallbackTypes = registrars.keySet();
        HashSet<Class> implementedCallbackTypes = new HashSet<Class>(Arrays.asList(callback.getClass().getInterfaces()));
        return Sets.intersection(knownCallbackTypes, implementedCallbackTypes);
    }

    private static HashMap<Class<?>, Registrar> prepareRegistrars() {
        HashMap<Class<?>, Registrar> registrars = new HashMap<Class<?>, Registrar>();
        registrars.put(UserDetailsCallback.class, new Registrar() {
            @Override
            public void register(VodafoneCallback callback) {
                // TODO register
            }

            @Override
            public void unregister(VodafoneCallback callback) {
                // TODO unregister
            }
        });
        registrars.put(ValidateSmsCallback.class, new Registrar() {
            @Override
            public void register(VodafoneCallback callback) {
                // TODO register
            }

            @Override
            public void unregister(VodafoneCallback callback) {
                // TODO unregister
            }
        });
        return registrars;
    }
}
