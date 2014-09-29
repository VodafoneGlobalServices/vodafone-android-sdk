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
import com.vodafone.global.sdk.http.worker.*;
import timber.log.Timber;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.vodafone.global.sdk.MessageType.*;

public class VodafoneManager {
    private static HashMap<Class<?>, Registrar> registrars;
    private final OkHttpClient client;

    private final Context context;
    private final String clientAppKey;
    private final String clientAppSecret;
    private final String backendAppKey;

    private final Settings settings;
    private final SpiceManager spiceManager;
    private final Worker worker;

    private final ResolveUserProcessor resolveUserProc;
    private final CheckStatusProcessor checkStatusProc;
    private final GeneratePinProcessor generatePinProc;
    private final ValidatePinProcessor ValidatePinProc;

    Set<ResolutionCallback> resolutionCallbacks = new CopyOnWriteArraySet<ResolutionCallback>();
    Set<ValidateSmsCallback> validateSmsCallbacks = new CopyOnWriteArraySet<ValidateSmsCallback>();
    private IMSI imsi;
    private Optional<OAuthToken> authToken = Optional.absent();
    private Optional<UserDetails> cachedUserDetails = Optional.absent();

    private MaximumThresholdChecker tresholdChecker;

    /**
     * Initializes SDK Manager for a given application.
     *
     * @param context android's context
     * @param clientAppKey application's identification
     */
    public VodafoneManager(Context context, String clientAppKey, String clientAppSecret, String backendAppKey) {
        this.context = context;

        //Application keys
        this.clientAppKey = clientAppKey;
        this.clientAppSecret = clientAppSecret;
        this.backendAppKey = backendAppKey;

        registrars = prepareRegistrars();
        client = new OkHttpClient();
        imsi = new IMSI(context);
        settings = new Settings(context);
        register(new CacheResolutionCallback());
        spiceManager = new SpiceManager(VodafoneService.class);
        spiceManager.start(this.context);

        worker = new Worker(callback);
        RequestBuilderProvider requestBuilderProvider = new RequestBuilderProvider(settings.sdkId, Utils.getAndroidId(context), Utils.getMCC(context), backendAppKey);
        resolveUserProc = new ResolveUserProcessor(context, worker, settings, backendAppKey, imsi, resolutionCallbacks, requestBuilderProvider);
        checkStatusProc = new CheckStatusProcessor(context, worker, settings, backendAppKey, imsi, resolutionCallbacks, requestBuilderProvider);
        generatePinProc = new GeneratePinProcessor(context, worker, settings, backendAppKey, validateSmsCallbacks, requestBuilderProvider);
        ValidatePinProc = new ValidatePinProcessor(context, worker, settings, backendAppKey, resolutionCallbacks, requestBuilderProvider);

        tresholdChecker = new MaximumThresholdChecker(settings.requestsThrottlingLimit, settings.requestsThrottlingPeriod);

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

        registrars.put(ResolutionCallback.class, new Registrar() {
            @Override
            public void register(VodafoneCallback callback) {
                resolutionCallbacks.add((ResolutionCallback) callback);
            }

            @Override
            public void unregister(VodafoneCallback callback) {
                resolutionCallbacks.remove(callback);
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
        worker.sendMessage(worker.createMessage(RETRIEVE_USER_DETAILS, parameters));
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
        worker.sendMessage(worker.createMessage(VALIDATE_PIN, parameters));
    }

    /**
     * Validates identity by providing code send by server via SMS.
     */
    public void generatePin() {
        Timber.d("generate pin");
        worker.sendMessage(worker.createMessage(GENERATE_PIN, cachedUserDetails.get().token));
    }

    /**
     * Callback used internally to cache UserDetails.
     */
    private class CacheResolutionCallback implements ResolutionCallback {
        @Override
        public void onCompleted(UserDetails userDetails) {
            cachedUserDetails = Optional.of(userDetails);
        }

        @Override
        public void onValidationRequired() {
        }

        @Override
        public void onFailed() {
            clearCache();
        }

        @Override
        public void onError(VodafoneException ex) {
            clearCache();
        }

        private void clearCache() {
            cachedUserDetails = Optional.absent();
        }
    }

    private Handler.Callback callback  = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            MessageType id = MessageType.values()[msg.what];
            switch (id) {
                case AUTHENTICATE:
                    authenticate();
                    break;
                case RETRIEVE_USER_DETAILS:
                    resolveUserProc.process(authToken, msg);
                    break;
                case CHECK_STATUS:
                    checkStatusProc.process(authToken, msg);
                    break;
                case GENERATE_PIN:
                    generatePinProc.process(authToken, msg);
                    break;
                case VALIDATE_PIN:
                    ValidatePinProc.process(authToken, msg);
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
                .clientId(clientAppKey)
                .clientSecret(clientAppSecret)
                .scope(settings.oAuthTokenScope)
                .grantType(settings.oAuthTokenGrantType)
                .build();

        request.setRetryPolicy(null);
        request.setOkHttpClient(new OkHttpClient());
        return request.loadDataFromNetwork();
    }
}
