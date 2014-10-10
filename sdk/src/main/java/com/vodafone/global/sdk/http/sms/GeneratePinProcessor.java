package com.vodafone.global.sdk.http.sms;

import android.content.Context;
import android.net.Uri;
import android.os.Message;
import com.google.common.base.Optional;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;
import com.vodafone.global.sdk.http.GenericServerError;
import com.vodafone.global.sdk.RequestBuilderProvider;
import com.vodafone.global.sdk.Settings;
import com.vodafone.global.sdk.ValidateSmsCallbacks;
import com.vodafone.global.sdk.http.oauth.OAuthToken;
import com.vodafone.global.sdk.Worker;
import org.json.JSONException;

import java.io.IOException;

public class GeneratePinProcessor {
    protected final Worker worker;
    protected final Settings settings;
    protected final Context context;
    private final ValidateSmsCallbacks validateSmsCallbacks;
    private final GeneratePinParser parser;
    private String backendAppKey;
    private Optional<OAuthToken> authToken;
    private RequestBuilderProvider requestBuilderProvider;

    public GeneratePinProcessor(
            Context context,
            Worker worker,
            Settings settings,
            String backendAppKey,
            ValidateSmsCallbacks validateSmsCallbacks,
            RequestBuilderProvider requestBuilderProvider
    ) {
        this.context = context;
        this.worker = worker;
        this.settings = settings;
        this.validateSmsCallbacks = validateSmsCallbacks;
        this.backendAppKey = backendAppKey;
        this.requestBuilderProvider = requestBuilderProvider;
        parser = new GeneratePinParser(validateSmsCallbacks);
    }

    public void process(Optional<OAuthToken> authToken, Message msg) {
        this.authToken = authToken;
        String token = (String) msg.obj;

        try {
            Response response = queryServer(token);
            parser.parseResponse(response);
        } catch (Exception e) {
            validateSmsCallbacks.notifyError(new GenericServerError(e));
        }
    }

    Response queryServer(String token) throws IOException, JSONException {
        PinRequest request = getRequest(token);

        request.setRetryPolicy(null);
        request.setOkHttpClient(new OkHttpClient());

        return request.loadDataFromNetwork();
    }

    private PinRequest getRequest(String token) {
        return PinRequest.builder()
                .url(getUrl(token))
                .accessToken(authToken.get().accessToken)
                .requestBuilderProvider(requestBuilderProvider)
                .build();
    }

    private String getUrl(String token) {
        return new Uri.Builder().scheme(settings.apix.protocol)
                .authority(settings.apix.host)
                .path(settings.apix.path)
                .appendPath(token)
                .appendPath("pins")
                .appendQueryParameter("backendId", backendAppKey)
                .build()
                .toString();
    }
}
