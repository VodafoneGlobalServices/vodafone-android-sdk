package com.vodafone.global.sdk;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.octo.android.robospice.SpiceManager;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.vodafone.global.sdk.http.VodafoneService;
import com.vodafone.global.sdk.http.oauth.OAuthToken;
import com.vodafone.global.sdk.http.oauth.OAuthTokenRequest;
import com.vodafone.global.sdk.http.resolve.UserDetailsDTO;
import com.vodafone.global.sdk.http.worker.CheckStatusRequest;
import com.vodafone.global.sdk.http.worker.GeneratePinRequest;
import com.vodafone.global.sdk.http.worker.ResolveUserRequest;
import com.vodafone.global.sdk.http.worker.Worker;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class VodafoneManager {
    private static HashMap<Class<?>, Registrar> registrars;
    private final OkHttpClient client;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String TAG = VodafoneManager.class.getSimpleName();

    private final static int RETRY_CALL_LIMIT = 5;
    private final static int RETRY_INTERVAL_LIMIT_MS = 5*60*1000; //5 minutes

    private final static Queue<Long> requestStack = new LinkedList<Long>();

    private final Context context;
    private final String appId;
    private final Settings settings;
    private final SpiceManager spiceManager;
    private final Worker worker;

    private final ResolveUserRequest userRequest;
    private final CheckStatusRequest statusRequest;
    private final GeneratePinRequest pinRequest;

    Set<UserDetailsCallback> userDetailsCallbacks = new CopyOnWriteArraySet<UserDetailsCallback>();
    Set<ValidateSmsCallback> validateSmsCallbacks = new CopyOnWriteArraySet<ValidateSmsCallback>();
    private SimSerialNumber iccid;
    private Optional<OAuthToken> authToken = Optional.absent();
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
        userRequest = new ResolveUserRequest(context, settings, appId, iccid, userDetailsCallbacks);
        statusRequest = new CheckStatusRequest(context, settings, appId, iccid, userDetailsCallbacks);
        pinRequest = new GeneratePinRequest(context, settings, appId, iccid, userDetailsCallbacks);
        worker = new Worker(callback);
        worker.start();
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
        worker.sendMessage(worker.createMessage(MESSAGE_ID.RETRIEVE_USER_DETAILS.ordinal(), parameters));
    }

    /**
     * Validates identity by providing code send by server via SMS.
     *
     * @param code code send to user via SMS
     */
    public void validateSmsCode(String code) {

    }

    /**
     * Validates identity by providing code send by server via SMS.
     *
     * @param validatePinParameters code send to user via SMS
     */
    public void generatePin(UserDetails userDetails) {
        worker.sendMessage(worker.createMessage(MESSAGE_ID.GENERATE_PIN.ordinal(), userDetails));
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

    private boolean isMaximumThresholdReached() {
        boolean maximumThresholdReach = true;
        Long currentTime = System.currentTimeMillis();

        while (requestStack.peek() < (currentTime - RETRY_INTERVAL_LIMIT_MS)) {
            requestStack.remove();
        }

        if (RETRY_CALL_LIMIT < requestStack.size()) {
            maximumThresholdReach = false;
        }
        requestStack.add(currentTime);
        return maximumThresholdReach;
    }


    public enum MESSAGE_ID {
        RETRIEVE_USER_DETAILS,
        AUTHENTICATE,
        CHECK_STATUS,
        REDIRECT,
        GENERATE_PIN
    }

    private Handler.Callback callback  = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            MESSAGE_ID id = MESSAGE_ID.values()[msg.what];
            switch (id) {
                case AUTHENTICATE:
                    authenticate();
                    break;
                case RETRIEVE_USER_DETAILS:
                    userRequest.process(worker, authToken, msg);
                    break;
                case REDIRECT:
                    redirect(msg);
                   break;
                case CHECK_STATUS:
                    userRequest.process(worker, authToken, msg);
                    break;
                case GENERATE_PIN:
                    pinRequest.process(worker, authToken, msg);
                    break;
                default:
                    return false;
            }
            return true;
        }
    };

    private void redirect(Message msg) {
        UserDetailsDTO redirectDetails = (UserDetailsDTO) msg.obj;
        if (redirectDetails.userDetails.validationRequired) {
            //TODO notify listeners
        } else {
            //START CHECK STATUS WITH DELAY
            worker.sendMessageDelayed(worker.createMessage(MESSAGE_ID.CHECK_STATUS.ordinal(), redirectDetails), redirectDetails.retryAfter);
        }
    }

    private void authenticate() {
        try {
            authToken = Optional.of(retrieveOAuthToken());
        } catch (Exception e) {
            //TODO: ERROR authentication failure
        }
    }

    private OAuthToken retrieveOAuthToken() throws Exception {
        Uri.Builder builder = new Uri.Builder();
        Uri uri = builder.scheme(settings.oauth.protocol).authority(settings.oauth.host).path(settings.oauth.path).build();
        OAuthTokenRequest request = OAuthTokenRequest.builder()
                .url(uri.toString())
                .clientId(settings.oAuthTokenClientId)
                .clientSecret(settings.oAuthTokenClientSecret)
                .scope(settings.oAuthTokenScope)
                .grantType(settings.oAuthTokenGrantType)
                .build();

        request.setRetryPolicy(null);
        request.setOkHttpClient(new OkHttpClient());
        return request.loadDataFromNetwork();
    }
}
