package com.vodafone.global.sdk.http.worker;

import android.content.Context;
import android.net.Uri;
import android.os.Message;
import com.google.common.base.Optional;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;
import com.vodafone.global.sdk.*;
import com.vodafone.global.sdk.http.oauth.OAuthToken;
import com.vodafone.global.sdk.http.sms.ValidatePinRequestDirect;
import org.json.JSONException;

import java.io.IOException;

public class ValidatePinProcessor {
    protected final Worker worker;
    protected final Settings settings;
    protected final Context context;
    protected final ResolveCallbacks resolveCallbacks;
    private final ValidatePinParser parser;
    private String backendAppKey;
    private final RequestBuilderProvider requestBuilderProvider;
    private Optional<OAuthToken> authToken;

    public ValidatePinProcessor(
            Context context,
            Worker worker,
            Settings settings,
            String backendAppKey,
            ResolveCallbacks resolveCallbacks,
            RequestBuilderProvider requestBuilderProvider
    ) {
        this.context = context;
        this.worker = worker;
        this.settings = settings;
        this.resolveCallbacks = resolveCallbacks;
        this.backendAppKey = backendAppKey;
        this.requestBuilderProvider = requestBuilderProvider;
        parser = new ValidatePinParser(resolveCallbacks);
    }

    public void process(Optional<OAuthToken> authToken, Message msg) {
        this.authToken = authToken;
        ValidatePinParameters validatePinParameters = (ValidatePinParameters) msg.obj;

        try {
            Response response = queryServer(validatePinParameters);
            parser.parseResponse(response);
        } catch (Exception e) {
            resolveCallbacks.notifyError(new GenericServerError());
        }
    }

    Response queryServer(ValidatePinParameters validatePinParameters) throws IOException, JSONException {
        ValidatePinRequestDirect request = getRequest(validatePinParameters);

        request.setRetryPolicy(null);
        request.setOkHttpClient(new OkHttpClient());

        return request.loadDataFromNetwork();
    }

    private ValidatePinRequestDirect getRequest(ValidatePinParameters validatePinParameters) {
        return ValidatePinRequestDirect.builder()
                .url(getUrl(validatePinParameters))
                .accessToken(authToken.get().accessToken)
                .pin(validatePinParameters.getPin())
                .requestBuilderProvider(requestBuilderProvider)
                .build();
    }

    private String getUrl(ValidatePinParameters validatePinParameters) {
        return new Uri.Builder().scheme(settings.apix.protocol)
                .authority(settings.apix.host)
                .path(settings.apix.path)
                .appendPath(validatePinParameters.getToken())
                .appendPath("pins")
                .appendQueryParameter("backendId", backendAppKey).build().toString();
    }
}
