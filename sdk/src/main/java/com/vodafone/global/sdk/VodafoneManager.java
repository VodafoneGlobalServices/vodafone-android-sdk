package com.vodafone.global.sdk;

import android.content.Context;
import com.google.common.collect.Sets;
import com.squareup.okhttp.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

class VodafoneManager {
    private static HashMap<Class<?>, Registrar> registrars;
    private final OkHttpClient client;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final String appId;

    List<UserDetailsCallback> userDetailsCallbacks = new CopyOnWriteArrayList<UserDetailsCallback>();
    List<ValidateSmsCallback> validateSmsCallbacks = new CopyOnWriteArrayList<ValidateSmsCallback>();
    private String sessionToken;
    private String iccid;

    public VodafoneManager(Context context, String appId) {
        this.appId = appId;
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
        String payload = prepareRetrievePayload(parameters);
        RequestBody body = RequestBody.create(JSON, payload);

        Request request = new Request.Builder()
                .url("http://hebemock-4953648878.eu-de1.plex.vodafone.com/users/resolve")
                .post(body)
                .build();

        client.newCall(request).enqueue(new UserDetailsResponseCallback(userDetailsCallbacks));
    }

    private String prepareRetrievePayload(UserDetailsRequestParameters parameters) {
        try {
            JSONObject json = new JSONObject();
            json.put("applicationId", appId);
            json.put("sessionToken", sessionToken);
            json.put("iccid", iccid);
            json.put("smsValidation", parameters.smsValidation());
            return json.toString();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void validateSmsCode(String code) {
        String payload = prepareSmsValidationPayload(code);
        RequestBody body = RequestBody.create(JSON, payload);

        Request request = new Request.Builder()
                .url("http://hebemock-4953648878.eu-de1.plex.vodafone.com/users/tokens/validate/{token}")
                .post(body)
                .build();

        client.newCall(request).enqueue(new ValidateSmsResponseCallback(validateSmsCallbacks));
    }

    private String prepareSmsValidationPayload(String code) {
        try {
            JSONObject json = new JSONObject();
            json.put("code", code);
            return json.toString();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
