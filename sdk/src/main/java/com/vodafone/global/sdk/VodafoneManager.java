package com.vodafone.global.sdk;

import android.content.Context;
import com.google.common.collect.Sets;
import com.squareup.okhttp.*;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

class VodafoneManager {
    private static HashMap<Class<?>, Registrar> registrars;
    private final OkHttpClient client;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    List<UserDetailsCallback> userDetailsCallbacks = new CopyOnWriteArrayList<UserDetailsCallback>();
    List<ValidateSmsCallback> validateSmsCallbacks = new CopyOnWriteArrayList<ValidateSmsCallback>();

    public VodafoneManager(Context context) {
        registrars = prepareRegistrars();
        client = new OkHttpClient();
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
                userDetailsCallbacks.add((UserDetailsCallback) callback);
            }

            @Override
            public void unregister(VodafoneCallback callback) {
                userDetailsCallbacks.remove(callback);
            }
        });

        registrars.put(ValidateSmsCallback.class, new Registrar() {
            @Override
            public void register(VodafoneCallback callback) {
                validateSmsCallbacks.add((ValidateSmsCallback) callback);
            }

            @Override
            public void unregister(VodafoneCallback callback) {
                validateSmsCallbacks.remove(callback);
            }
        });

        return registrars;
    }

    public UserDetails getUserDetails() {
        // TODO return cached object
        return null;
    }

    public void retrieveUserDetails(final UserDetailsRequestParameters parameters) {
        String json = "";
        RequestBody body = RequestBody.create(JSON, json);

        Request request = new Request.Builder()
                .url("http://hebemock-4953648878.eu-de1.plex.vodafone.com/users/resolve")
                .post(body)
                .build();

        client.newCall(request).enqueue(new UserDetailsResponseCallback(userDetailsCallbacks));
    }

    public void validateSmsCode(String code) {
        String json = "{code:" + code + "}"; // TODO escaping
        RequestBody body = RequestBody.create(JSON, json);

        Request request = new Request.Builder()
                .url("http://hebemock-4953648878.eu-de1.plex.vodafone.com/users/tokens/validate/{token}")
                .post(body)
                .build();

        client.newCall(request).enqueue(new ValidateSmsResponseCallback(validateSmsCallbacks));
    }
}
