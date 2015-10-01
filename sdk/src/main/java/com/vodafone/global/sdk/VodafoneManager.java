package com.vodafone.global.sdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.squareup.okhttp.OkHttpClient;
import com.vodafone.global.sdk.http.GenericServerError;
import com.vodafone.global.sdk.http.oauth.AuthorizationFailed;
import com.vodafone.global.sdk.http.oauth.OAuthProcessor;
import com.vodafone.global.sdk.http.oauth.OAuthToken;
import com.vodafone.global.sdk.http.resolve.CheckStatusProcessor;
import com.vodafone.global.sdk.http.resolve.ResolveUserProcessor;
import com.vodafone.global.sdk.http.settings.UpdateSettingsProcessor;
import com.vodafone.global.sdk.http.sms.*;
import com.vodafone.global.sdk.logging.Logger;
import com.vodafone.global.sdk.logging.LoggerFactory;
import org.json.JSONException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.vodafone.global.sdk.MessageType.*;

public class VodafoneManager {
    private static HashMap<Class<?>, Registrar> registrars;

    private final Context context;
    private final String clientAppKey;
    private final String clientAppSecret;
    private final String backendAppKey;

    private final Worker worker;
    private final VodafoneManager.OnSmsInterceptionTimeoutCallback smsInterceptionTimeoutCallback;
    private Settings settings;

    private UpdateSettingsProcessor updateSettingsProc;
    private OAuthProcessor oAuthProc;
    private ResolveUserProcessor resolveUserProc;
    private CheckStatusProcessor checkStatusProc;
    private GeneratePinProcessor generatePinProc;
    private ValidatePinProcessor validatePinProc;

    ResolveCallbacks resolveCallbacks = new ResolveCallbacks();
    ValidateSmsCallbacks validateSmsCallbacks;
    private Optional<OAuthToken> authToken = Optional.absent();

    private MaximumThresholdChecker retrieveThresholdChecker;
    private MaximumThresholdChecker genPinThresholdChecker;
    private MaximumThresholdChecker validatePinThresholdChecker;
    private final Logger logger;
    private OkHttpClient httpClient;
    private SmsInboxObserver smsReceiver;

    /**
     * Initializes SDK Manager for a given application.
     *
     * @param context android's context
     * @param clientAppKey application's identification
     */
    public VodafoneManager(Context context, String clientAppKey, String clientAppSecret, String backendAppKey) {
        this.context = context;
        validateSmsCallbacks = new ValidateSmsCallbacks(context);

        //Application keys
        this.clientAppKey = clientAppKey;
        this.clientAppSecret = clientAppSecret;
        this.backendAppKey = backendAppKey;

        logger = LoggerFactory.getLogger();
        registrars = prepareRegistrars();

        httpClient = new OkHttpClient();
        worker = new Worker(callback);

        smsInterceptionTimeoutCallback = new VodafoneManager.OnSmsInterceptionTimeoutCallback();

        settings = readSettings(context);
        init(context, settings, clientAppKey, clientAppSecret, backendAppKey);

        worker.start();
    }

    private Settings readSettings(Context context) {
        Settings settings;
        SharedPreferences preferences = context.getSharedPreferences(Settings.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        String json = preferences.getString(Settings.SETTINGS_JSON, "");
        if (json.isEmpty()) {
            settings = new Settings(context);
            logger.d("using internal settings");
        } else {
            try {
                settings = new Settings(json);
                logger.d("using settings from server");
            } catch (JSONException e) {
                settings = new Settings(context);
                logger.d("using internal settings");
            }
        }
        return settings;
    }

    private void init(Context context, Settings settings, String clientAppKey, String clientAppSecret, String backendAppKey) {
        httpClient.setReadTimeout(settings.defaultHttpConnectionTimeout(), TimeUnit.SECONDS);
        IMSI imsi = new IMSI(context, settings.availableMccMnc());
        RequestBuilderProvider requestBuilderProvider = new RequestBuilderProvider(settings.sdkId(), Utils.getAndroidId(context), Utils.getMCC(context), backendAppKey, clientAppKey);
        updateSettingsProc = new UpdateSettingsProcessor(context, httpClient, requestBuilderProvider,logger);
        oAuthProc = new OAuthProcessor(httpClient, clientAppKey, clientAppSecret, settings, logger);
        resolveUserProc = new ResolveUserProcessor(context, httpClient, worker, settings, backendAppKey, imsi, resolveCallbacks, requestBuilderProvider, logger);
        checkStatusProc = new CheckStatusProcessor(context, httpClient, worker, settings, backendAppKey, resolveCallbacks, requestBuilderProvider, logger);
        generatePinProc = new GeneratePinProcessor(context, httpClient, worker, settings, backendAppKey, resolveCallbacks, validateSmsCallbacks, requestBuilderProvider, logger);
        validatePinProc = new ValidatePinProcessor(context, httpClient, worker, settings, backendAppKey, resolveCallbacks, validateSmsCallbacks, requestBuilderProvider, logger);

        retrieveThresholdChecker = new MaximumThresholdChecker(settings.requestsThrottlingLimit(), settings.requestsThrottlingPeriod());
        genPinThresholdChecker = new MaximumThresholdChecker(settings.requestsThrottlingLimit(), settings.requestsThrottlingPeriod());
        validatePinThresholdChecker = new MaximumThresholdChecker(settings.requestsThrottlingLimit(), settings.requestsThrottlingPeriod());


        smsReceiver = new SmsInboxObserver(context, settings, smsInterceptionTimeoutCallback);
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
        long currentTimeMillis = System.currentTimeMillis();
        SharedPreferences preferences = context.getSharedPreferences(Settings.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        long configurationExpiresAtTimeMillis = preferences.getLong(Settings.EXPIREST_AT, currentTimeMillis);
        if (currentTimeMillis >= configurationExpiresAtTimeMillis) {
            worker.sendMessage(worker.createMessage(UPDATE_SETTINGS));
        } else {
            logger.d("Skipping conf update. Time left until next update: %d ms",
                    configurationExpiresAtTimeMillis - currentTimeMillis);
        }

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

        sendPin(code);
    }

    private void sendPin(String code) {
        Optional<String> sessionToken = resolveCallbacks.getSessionToken();
        if (!sessionToken.isPresent()) {
            logger.w("session is missing, ignoring pin: " + code);
            return;
        }

        logger.d("received pin: " + code);

        if (code.matches(settings.pinParameterValidationRegexp())) {
            ValidatePinParameters parameters = ValidatePinParameters.builder()
                    .token(sessionToken.get())
                    .pin(code)
                    .build();
            worker.sendMessage(worker.createMessage(VALIDATE_PIN, parameters));
        } else {
            logger.w("pin " + code + " does not match " + settings.pinParameterValidationRegexp());

            validateSmsCallbacks.notifyError(new InvalidInput("Invalid PIN"));
        }
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

    private boolean intercepting;
    private Handler.Callback callback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            try {
                MessageType id = MessageType.values()[msg.what];
                switch (id) {
                    case UPDATE_SETTINGS:
                        logger.d("START Settings update");
                        try {
                            Settings newSettings = updateSettingsProc.process();
                            if (newSettings != null) {
                                init(context, newSettings, clientAppKey, clientAppSecret, backendAppKey);
                                settings = newSettings;
                            }
                        } catch (Exception e) {
                            logger.e(e, "An exception occurred during settings update");
                        }
                        break;
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
                        if (Utils.canInterceptSms(context)) {
                            logger.d("can intercept");
                            intercepting = true;
                        }
                        break;
                    case CHECK_STATUS:
                        logger.d("START Check status");
                        checkStatusProc.process(authToken, msg);
                        break;
                    case GENERATE_PIN:
                        logger.d("START Generate pin");
                        GeneratePinParser generatePinParser;
                        if (intercepting) {
                            generatePinParser = GeneratePinParser.withInterception(worker, resolveCallbacks, validateSmsCallbacks, smsReceiver);
                            logger.w("can intercept");
                        } else {
                            generatePinParser = GeneratePinParser.withoutInterception(worker, resolveCallbacks, validateSmsCallbacks);
                        }
                        generatePinProc.process(generatePinParser, authToken, msg);
                        break;
                    case VALIDATE_PIN:
                        logger.d("START Validate pin");
                        ValidatePinParser parser;
                        if (intercepting) {
                            parser = ValidatePinParser.withInterception(worker, resolveCallbacks, validateSmsCallbacks);
                            intercepting = false;
                        } else {
                            parser = ValidatePinParser.withoutInterception(worker, resolveCallbacks, validateSmsCallbacks);
                        }
                        validatePinProc.process(parser, authToken, msg);
                        break;
                    default:
                        return false;
                }
                return true;
            } catch (Exception e) {
                worker.clearMessageQueue();
                resolveCallbacks.notifyError(new GenericServerError(e.getMessage(), e));
                return false;
            }
        }
    };

    private class OnSmsInterceptionTimeoutCallback
            implements SmsInboxObserver.OnSmsInterceptionTimeoutCallback
    {
        @Override
        public void onTimeout() {
            intercepting = false;
            if (resolveCallbacks.getSessionToken().isPresent()) {
                resolveCallbacks.validationRequired(resolveCallbacks.getSessionToken().get());
            }
        }

        @Override
        public void validateSmsCode(String pin) {
            intercepting = false;
            sendPin(pin);
        }
    }
}
