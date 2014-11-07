package com.vodafone.global.sdk;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.vodafone.global.sdk.http.oauth.AuthorizationFailed;
import com.vodafone.global.sdk.http.oauth.OAuthProcessor;
import com.vodafone.global.sdk.http.oauth.OAuthToken;
import com.vodafone.global.sdk.http.resolve.CheckStatusProcessor;
import com.vodafone.global.sdk.http.resolve.ResolveUserProcessor;
import com.vodafone.global.sdk.http.sms.GeneratePinProcessor;
import com.vodafone.global.sdk.http.sms.ValidatePinProcessor;
import com.vodafone.global.sdk.logging.Logger;
import com.vodafone.global.sdk.logging.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static com.vodafone.global.sdk.MessageType.*;

public class VodafoneManager {
    private static HashMap<Class<?>, Registrar> registrars;

    private final Context context;
    private final String clientAppKey;
    private final String clientAppSecret;
    private final String backendAppKey;

    private final Settings settings;
    private final Worker worker;

    private final OAuthProcessor oAuthProc;
    private final ResolveUserProcessor resolveUserProc;
    private final CheckStatusProcessor checkStatusProc;
    private final GeneratePinProcessor generatePinProc;
    private final ValidatePinProcessor validatePinProc;

    ResolveCallbacks resolveCallbacks = new ResolveCallbacks();
    ValidateSmsCallbacks validateSmsCallbacks = new ValidateSmsCallbacks();
    private IMSI imsi;
    private Optional<OAuthToken> authToken = Optional.absent();

    private MaximumThresholdChecker retrieveThresholdChecker;
    private final MaximumThresholdChecker genPinThresholdChecker;
    private final MaximumThresholdChecker validatePinThresholdChecker;
    private final Logger logger;

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
        settings = new Settings(context);
        imsi = new IMSI(context, settings.availableMccMnc);

        worker = new Worker(callback);
        logger = LoggerFactory.getLogger();
        RequestBuilderProvider requestBuilderProvider = new RequestBuilderProvider(settings.sdkId, Utils.getAndroidId(context), Utils.getMCC(context), backendAppKey, clientAppKey);
        oAuthProc = new OAuthProcessor(clientAppKey, clientAppSecret, settings);
        resolveUserProc = new ResolveUserProcessor(context, worker, settings, backendAppKey, imsi, resolveCallbacks, requestBuilderProvider, logger);
        checkStatusProc = new CheckStatusProcessor(context, worker, settings, backendAppKey, resolveCallbacks, requestBuilderProvider, logger);
        generatePinProc = new GeneratePinProcessor(context, worker, settings, backendAppKey, resolveCallbacks, validateSmsCallbacks, requestBuilderProvider, logger);
        validatePinProc = new ValidatePinProcessor(context, worker, settings, backendAppKey, resolveCallbacks, validateSmsCallbacks, requestBuilderProvider, logger);

        retrieveThresholdChecker = new MaximumThresholdChecker(settings.requestsThrottlingLimit, settings.requestsThrottlingPeriod);
        genPinThresholdChecker = new MaximumThresholdChecker(settings.requestsThrottlingLimit, settings.requestsThrottlingPeriod);
        validatePinThresholdChecker = new MaximumThresholdChecker(settings.requestsThrottlingLimit, settings.requestsThrottlingPeriod);

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

        registrars.put(ResolveCallback.class, new Registrar() {
            @Override
            public void register(VodafoneCallback callback) {
                resolveCallbacks.add((ResolveCallback) callback);
            }

            @Override
            public void unregister(VodafoneCallback callback) {
                resolveCallbacks.remove((ResolveCallback) callback);
            }
        });

        registrars.put(ValidateSmsCallback.class, new Registrar() {
            @Override
            public void register(VodafoneCallback callback) {
                validateSmsCallbacks.add((ValidateSmsCallback) callback);
            }

            @Override
            public void unregister(VodafoneCallback callback) {
                validateSmsCallbacks.remove((ValidateSmsCallback) callback);
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
        if (retrieveThresholdChecker.thresholdReached()) {
            throw new CallThresholdReached();
        }
        worker.sendMessage(worker.createMessage(RETRIEVE_USER_DETAILS, parameters));
    }

    /**
     * Validates identity by providing code send by server via SMS.
     *
     * @param code code send to user via SMS
     */
    public void validateSmsCode(String code) {
        if (validatePinThresholdChecker.thresholdReached()) {
            throw new CallThresholdReached();
        }
        Optional<String> sessionToken = resolveCallbacks.getSessionToken();
        if (!sessionToken.isPresent()) {
            logger.w("Vodafone", "session is missing, ignoring pin: " + code);
            return;
        }

        logger.d("Vodafone", "received sms");

        ValidatePinParameters parameters = ValidatePinParameters.builder()
                .token(sessionToken.get())
                .pin(code).build();
        worker.sendMessage(worker.createMessage(VALIDATE_PIN, parameters));
    }

    /**
     * Validates identity by providing code send by server via SMS.
     */
    public void generatePin() {
        if (genPinThresholdChecker.thresholdReached()) {
            throw new CallThresholdReached();
        }
        Optional<String> sessionToken = resolveCallbacks.getSessionToken();
        if (!sessionToken.isPresent()) {
            return;
        }

        worker.sendMessage(worker.createMessage(GENERATE_PIN, sessionToken.get()));
    }

    private Handler.Callback callback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            try {
                MessageType id = MessageType.values()[msg.what];
                switch (id) {
                    case AUTHENTICATE:
                        logger.d("START Authenticate");
                        try {
                            authToken = Optional.of(oAuthProc.process());
                        } catch (AuthorizationFailed e) {
                            worker.clearMessageQueue();
                            resolveCallbacks.notifyError(e);
                        }
                        break;
                    case RETRIEVE_USER_DETAILS:
                        logger.d("START Retrieve user details");
                        resolveUserProc.process(authToken, msg);
                        break;
                    case CHECK_STATUS:
                        logger.d("START Check status");
                        checkStatusProc.process(authToken, msg);
                        break;
                    case GENERATE_PIN:
                        logger.d("START Generate pin");
                        generatePinProc.process(authToken, msg);
                        break;
                    case VALIDATE_PIN:
                        logger.d("START Validate pin");
                        validatePinProc.process(authToken, msg);
                        break;
                    default:
                        return false;
                }
                return true;
            } catch (Exception e) {
                worker.clearMessageQueue();
                logger.e(e, "Uncaught exception");
                return false;
            }
        }
    };
}
