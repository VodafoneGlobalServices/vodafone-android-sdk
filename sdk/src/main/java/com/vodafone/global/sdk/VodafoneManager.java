package com.vodafone.global.sdk;

import android.content.Context;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.octo.android.robospice.SpiceManager;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.vodafone.global.sdk.http.VodafoneService;
import com.vodafone.global.sdk.http.resolve.ResolvePostRequest;
import com.vodafone.global.sdk.http.resolve.ResolvePostRequestListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

class VodafoneManager {
    private static HashMap<Class<?>, Registrar> registrars;
    private final OkHttpClient client;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final Context context;
    private final String appId;
    private final Settings settings;
    private final SpiceManager spiceManager;

    Set<UserDetailsCallback> userDetailsCallbacks = new CopyOnWriteArraySet<UserDetailsCallback>();
    Set<ValidateSmsCallback> validateSmsCallbacks = new CopyOnWriteArraySet<ValidateSmsCallback>();
    private SimSerialNumber iccid;
    private Optional<UserDetails> cachedUserDetails = Optional.absent();

    /**
     * Initializes SDK Manager for a given application.
     *
     * @param context android's context
     * @param appId application's identification
     */
    public VodafoneManager(Context context, String appId) {
        this.context = context;
        this.appId = appId;
        registrars = prepareRegistrars();
        client = new OkHttpClient();
        iccid = new SimSerialNumber(context);
        settings = new Settings(context);
        register(new CacheUserDetailsCallback());
        spiceManager = new SpiceManager(VodafoneService.class);
        spiceManager.start(this.context);
    }

    /**
     * Used to register callbacks.
     *
     * @throws IllegalArgumentException if callback is of unknown type
     */
    public void register(VodafoneCallback callback) {
        Sets.SetView<Class<?>> knownAndImplementedCallbacksTypes = getKnownAndImplementedCallbacksTypes(callback);

        if (knownAndImplementedCallbacksTypes.isEmpty())
            throw new IllegalArgumentException("Unknown type of callback");

        for (Class c : knownAndImplementedCallbacksTypes)
            registrars.get(c).register(callback);
    }

    /**
     * Used to unregister callback.
     *
     * @param callback callback to be unregistered
     * @throws IllegalArgumentException if callback is of unknown type
     */
    public void unregister(VodafoneCallback callback) {
        Sets.SetView<Class<?>> knownAndImplementedCallbacksTypes = getKnownAndImplementedCallbacksTypes(callback);

        if (knownAndImplementedCallbacksTypes.isEmpty())
            throw new IllegalArgumentException("Unknown type of callback");

        for (Class c : knownAndImplementedCallbacksTypes)
            registrars.get(c).unregister(callback);
    }

    /**
     * Creates intersection of supported callback types and the ones implemented by passed object.
     * @param callback might implement more than one callback type
     */
    private Sets.SetView<Class<?>> getKnownAndImplementedCallbacksTypes(VodafoneCallback callback) {
        Set<Class<?>> knownCallbackTypes = registrars.keySet();
        HashSet<Class> implementedCallbackTypes = new HashSet<Class>(Arrays.asList(callback.getClass().getInterfaces()));
        return Sets.intersection(knownCallbackTypes, implementedCallbackTypes);
    }

    /**
     * Initializes registrars for every supported type of callback.
     */
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

    /**
     * Retrieves UserDetails from cache.
     * Returns immediately and returns cached object.
     *
     * @return cached object
     */
    public UserDetails getUserDetails() {
        return cachedUserDetails.orNull();
    }

    /**
     * Asynchronous call to backend to get user detail.
     *
     * @param parameters parameters specific to this call
     */
    public void retrieveUserDetails(final UserDetailsRequestParameters parameters) {
        String accessToken = "";
        String androidId = "";
        String mobileCountryCode = "";
        String sdkId = "";
        ResolvePostRequest request = ResolvePostRequest.builder()
                .url(settings.resolveOverWiFi.protocol + "://"
                        + settings.resolveOverWiFi.host
                        + settings.resolveOverWiFi.path)
                .accessToken(accessToken)
                .androidId(androidId)
                .mobileCountryCode(mobileCountryCode)
                .sdkId(sdkId)
                .appId(appId)
                .imsi(iccid)
                .smsValidation(false)
                .build();
        ResolvePostRequestListener requestListener = new ResolvePostRequestListener(spiceManager, userDetailsCallbacks);
        spiceManager.execute(request, requestListener);
    }

    /**
     * Validates identity by providing code send by server via SMS.
     *
     * @param code code send to user via SMS
     */
    public void validateSmsCode(String code) {
        // TODO adjust network communication to new requirements

        String payload = prepareSmsValidationPayload(code);
        RequestBody body = RequestBody.create(JSON, payload);

        Request request = new Request.Builder()
                .url(settings.validatePin.protocol + "://"
                        + settings.validatePin.host
                        + settings.validatePin.path
                        + "/" + cachedUserDetails.get().token)
                .post(body)
                .build();

        client.newCall(request).enqueue(new ValidateSmsResponseCallback(validateSmsCallbacks));
    }

    /**
     * Prepares JSON payload for sms validation.
     * @param code code from sms
     * @return JSON string
     */
    private String prepareSmsValidationPayload(String code) {
        try {
            JSONObject json = new JSONObject();
            json.put("code", code);
            return json.toString();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Callback used internally to cache UserDetails.
     */
    private class CacheUserDetailsCallback implements UserDetailsCallback {
        @Override
        public void onUserDetailsUpdate(UserDetails userDetails) {
            cachedUserDetails = Optional.of(userDetails);
        }

        @Override
        public void onUserDetailsError(VodafoneException ex) {
            cachedUserDetails = Optional.absent();
        }
    }
}
