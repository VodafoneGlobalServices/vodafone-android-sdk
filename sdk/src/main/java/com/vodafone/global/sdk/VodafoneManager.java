package com.vodafone.global.sdk;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.google.common.collect.Sets;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class VodafoneManager {
    /**
     * Handler used to invoke callbacks on main thread.
     */
    private final Handler handler;
    /**
     * Executor used to run requests.
     */
    private final ExecutorService executor;
    private static HashMap<Class<?>, Registrar> registrars;
    private Context context;

    public VodafoneManager(Context context) {
        this.context = context;
        executor = Executors.newSingleThreadExecutor(); // TODO better single thread executor
        handler = new Handler(Looper.getMainLooper());
        registrars = prepareRegistrars();
    }

    public void register(VodafoneCallback callback) {
        Sets.SetView<Class<?>> knownAndImplementedCallbacksTypes = getKnownAndImplementedCallbacksTypes(callback);

        if (knownAndImplementedCallbacksTypes.isEmpty())
            throw new IllegalArgumentException("Unknown type of callback");

        for (Class c : knownAndImplementedCallbacksTypes)
            registrars.get(c).register(callback);
    }

    public void unregister(VodafoneCallback callback) {
        Sets.SetView<Class<?>> knownAndImplementedCallbacksTypes = getKnownAndImplementedCallbacksTypes(callback);

        if (knownAndImplementedCallbacksTypes.isEmpty())
            throw new IllegalArgumentException("Unknown type of callback");

        for (Class c : knownAndImplementedCallbacksTypes)
            registrars.get(c).unregister(callback);
    }

    private Sets.SetView<Class<?>> getKnownAndImplementedCallbacksTypes(VodafoneCallback callback) {
        Set<Class<?>> knownCallbackTypes = registrars.keySet();
        HashSet<Class> implementedCallbackTypes = new HashSet<Class>(Arrays.asList(callback.getClass().getInterfaces()));
        return Sets.intersection(knownCallbackTypes, implementedCallbackTypes);
    }

    private HashMap<Class<?>, Registrar> prepareRegistrars() {
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

    public UserDetails getUserDetails() {
        // TODO return cached object
        return null;
    }

    public void retrieveUserDetails(final UserDetailsRequestParameters parameters) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                // TODO perform network request
            }
        });
    }

    public void validateSmsCode(String code) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                // TODO perform network request
            }
        });
    }
}
