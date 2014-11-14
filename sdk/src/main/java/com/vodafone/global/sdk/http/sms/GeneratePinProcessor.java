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

public class GeneratePinProcessor {
    private final OkHttpClient httpClient;
    protected final Worker worker;
    protected final Settings settings;
    protected final Context context;
    private final ValidateSmsCallbacks validateSmsCallbacks;
    private String backendAppKey;
    private Optional<OAuthToken> authToken;
    private RequestBuilderProvider requestBuilderProvider;
    private final Logger logger;

    public GeneratePinProcessor(
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
        this.validateSmsCallbacks = validateSmsCallbacks;
        this.backendAppKey = backendAppKey;
        this.requestBuilderProvider = requestBuilderProvider;
        this.logger = logger;
    }

    public void process(GeneratePinParser parser, Optional<OAuthToken> authToken, Message msg) {
        this.authToken = authToken;
        String token = (String) msg.obj;

        try {
            PinRequest request = getRequest(token);
            request.setOkHttpClient(httpClient);
            ResponseHolder response = request.loadDataFromNetwork();
            parser.parseResponse(response, token);
        } catch (Exception e) {
            validateSmsCallbacks.notifyError(new GenericServerError(e));
        }
    }

    private PinRequest getRequest(String token) {
        return PinRequest.builder()
                .url(getUrl(token))
                .accessToken(authToken.get().accessToken)
                .requestBuilderProvider(requestBuilderProvider)
                .logger(logger)
                .build();
    }

    private String getUrl(String token) {
        return new Uri.Builder()
                .scheme(settings.apix().protocol())
                .authority(settings.apix().host())
                .path(settings.apix().path())
                .appendPath(token)
                .appendPath("pins")
                .appendQueryParameter("backendId", backendAppKey)
                .build()
                .toString();
    }
}
