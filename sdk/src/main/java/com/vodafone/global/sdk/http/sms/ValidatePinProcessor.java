package com.vodafone.global.sdk.http.sms;

import android.content.Context;
import android.net.Uri;
import android.os.Message;
import com.google.common.base.Optional;
import com.squareup.okhttp.OkHttpClient;
import com.vodafone.global.sdk.*;
import com.vodafone.global.sdk.http.GenericServerError;
import com.vodafone.global.sdk.http.ResponseHolder;
import com.vodafone.global.sdk.http.oauth.OAuthToken;
import com.vodafone.global.sdk.logging.Logger;

public class ValidatePinProcessor {
    private final OkHttpClient httpClient;
    protected final Worker worker;
    protected final Settings settings;
    protected final Context context;
    protected final ResolveCallbacks resolveCallbacks;
    private String backendAppKey;
    private final RequestBuilderProvider requestBuilderProvider;
    private final Logger logger;
    private Optional<OAuthToken> authToken;

    public ValidatePinProcessor(
            Context context,
            OkHttpClient httpClient,
            Worker worker,
            Settings settings,
            String backendAppKey,
            ResolveCallbacks resolveCallbacks,
            ValidateSmsCallbacks validateSmsCallbacks,
            RequestBuilderProvider requestBuilderProvider,
            Logger logger
    ) {
        this.context = context;
        this.httpClient = httpClient;
        this.worker = worker;
        this.settings = settings;
        this.resolveCallbacks = resolveCallbacks;
        this.backendAppKey = backendAppKey;
        this.requestBuilderProvider = requestBuilderProvider;
        this.logger = logger;
    }

    public void process(ValidatePinParser parser, Optional<OAuthToken> authToken, Message msg) {
        this.authToken = authToken;
        ValidatePinParameters validatePinParameters = (ValidatePinParameters) msg.obj;

        try {
            ValidatePinRequest request = getRequest(validatePinParameters);

            request.setOkHttpClient(httpClient);

            ResponseHolder response = request.loadDataFromNetwork();
            parser.parseResponse(response, validatePinParameters.getToken(), validatePinParameters.getPin());
        } catch (Exception e) {
            resolveCallbacks.notifyError(new GenericServerError(e));
        }
    }

    private ValidatePinRequest getRequest(ValidatePinParameters validatePinParameters) {
        return ValidatePinRequest.builder()
                .url(getUrl(validatePinParameters))
                .accessToken(authToken.get().accessToken)
                .pin(validatePinParameters.getPin())
                .requestBuilderProvider(requestBuilderProvider)
                .logger(logger)
                .build();
    }

    private String getUrl(ValidatePinParameters validatePinParameters) {
        return new Uri.Builder()
                .scheme(settings.apix().protocol())
                .authority(settings.apix().host())
                .path(settings.apix().path())
                .appendPath(validatePinParameters.getToken())
                .appendPath("pins")
                .appendQueryParameter("backendId", backendAppKey)
                .build()
                .toString();
    }
}
