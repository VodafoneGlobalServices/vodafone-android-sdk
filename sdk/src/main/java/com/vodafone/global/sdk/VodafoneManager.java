package com.vodafone.global.sdk;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.octo.android.robospice.SpiceManager;
import com.squareup.okhttp.OkHttpClient;
import com.vodafone.global.sdk.http.VodafoneService;
import com.vodafone.global.sdk.http.oauth.OAuthToken;
import com.vodafone.global.sdk.http.oauth.OAuthTokenRequest;
import com.vodafone.global.sdk.http.worker.CheckStatusProcessor;
import com.vodafone.global.sdk.http.worker.GeneratePinProcessor;
import com.vodafone.global.sdk.http.worker.ResolveUserProcessor;
import com.vodafone.global.sdk.http.worker.ValidatePinProcessor;
import com.vodafone.global.sdk.http.worker.Worker;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import timber.log.Timber;

public class VodafoneManager {
    private static HashMap<Class<?>, Registrar> registrars;
    private final OkHttpClient client;

    private final Context context;
    private final String appKey;
    private final String appSecret;
    private final String backendAppKey;

    private final Settings settings;
    private final SpiceManager spiceManager;
    private final Worker worker;

    private final ResolveUserProcessor resolveUserProc;
    private final CheckStatusProcessor checkStatusProc;
    private final GeneratePinProcessor generatePinProc;
    private final ValidatePinProcessor ValidatePinProc;

    Set<UserDetailsCallback> userDetailsCallbacks = new CopyOnWriteArraySet<UserDetailsCallback>();
    Set<ValidateSmsCallback> validateSmsCallbacks = new CopyOnWriteArraySet<ValidateSmsCallback>();
    private SimSerialNumber iccid;
    private Optional<OAuthToken> authToken = Optional.absent();
    private Optional<UserDetails> cachedUserDetails = Optional.absent();

    private MaximumThresholdChecker tresholdChecker;

    /**
     * Initializes SDK Manager for a given application.
     *
     * @param context android's context
     * @param appKey application's identification
     */
    public VodafoneManager(Context context, String appKey, String appSecret, String backendAppKey) {
        this.context = context;

        //Application keys
        this.appKey = appKey;
        this.appSecret = appSecret;
        this.backendAppKey = backendAppKey;

        registrars = prepareRegistrars();
        client = new OkHttpClient();
        iccid = new SimSerialNumber(context);
        settings = new Settings(context);
        register(new CacheUserDetailsCallback());
        spiceManager = new SpiceManager(VodafoneService.class);
        spiceManager.start(this.context);

        resolveUserProc = new ResolveUserProcessor(context, settings, backendAppKey, iccid, userDetailsCallbacks);
        checkStatusProc = new CheckStatusProcessor(context, settings, backendAppKey, iccid, userDetailsCallbacks);
        generatePinProc = new GeneratePinProcessor(context, settings, backendAppKey, validateSmsCallbacks);
        ValidatePinProc = new ValidatePinProcessor(context, settings, backendAppKey, userDetailsCallbacks);

        tresholdChecker = new MaximumThresholdChecker(settings.requestsThrottlingLimit, settings.requestsThrottlingPeriod);

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
     * Asynchronous call to backend to get user detail.
     *
     * @param parameters parameters specific to this call
     */
    public void retrieveUserDetails(final UserDetailsRequestParameters parameters) {
        checkCallThreshold();
        worker.sendMessage(worker.createMessage(MESSAGE_ID.RETRIEVE_USER_DETAILS.ordinal(), parameters));
    }

    private void checkCallThreshold() {
        if (tresholdChecker.thresholdReached()) {
            throw new CallThresholdReached();
        }
    }

    /**
     * Validates identity by providing code send by server via SMS.
     *
     * @param code code send to user via SMS
     */
    public void validateSmsCode(String code) {
        ValidatePinParameters parameters = ValidatePinParameters.builder()
                .token(cachedUserDetails.get().token)
                .pin(code).build();
        worker.sendMessage(worker.createMessage(MESSAGE_ID.VALIDATE_PIN.ordinal(), parameters));
    }

    /**
     * Validates identity by providing code send by server via SMS.
     */
    public void generatePin() {
        Timber.d("generate pin");
        worker.sendMessage(worker.createMessage(MESSAGE_ID.GENERATE_PIN.ordinal(), cachedUserDetails.get().token));
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

    public enum MESSAGE_ID {
        RETRIEVE_USER_DETAILS,
        AUTHENTICATE,
        CHECK_STATUS,
        GENERATE_PIN,
        VALIDATE_PIN
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
                    resolveUserProc.process(worker, authToken, msg);
                    break;
                case CHECK_STATUS:
                    checkStatusProc.process(worker, authToken, msg);
                    break;
                case GENERATE_PIN:
                    generatePinProc.process(worker, authToken, msg);
                    break;
                case VALIDATE_PIN:
                    ValidatePinProc.process(worker, authToken, msg);
                    break;
                default:
                    return false;
            }
            return true;
        }
    };

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
                .clientId(appKey)
                .clientSecret(appSecret)
                .scope(settings.oAuthTokenScope)
                .grantType(settings.oAuthTokenGrantType)
                .build();

        request.setRetryPolicy(null);
        request.setOkHttpClient(new OkHttpClient());
        return request.loadDataFromNetwork();
    }
}
