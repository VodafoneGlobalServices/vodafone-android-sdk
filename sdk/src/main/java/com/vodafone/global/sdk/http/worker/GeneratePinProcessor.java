package com.vodafone.global.sdk.http.worker;

import android.content.Context;
import android.net.Uri;
import android.os.Message;
import com.google.common.base.Optional;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;
import com.vodafone.global.sdk.*;
import com.vodafone.global.sdk.http.oauth.OAuthToken;
import com.vodafone.global.sdk.http.sms.PinRequestDirect;
import org.json.JSONException;

import java.io.IOException;

import static com.vodafone.global.sdk.http.HttpCode.*;

public class GeneratePinProcessor {
    protected final Worker worker;
    protected final Settings settings;
    protected final Context context;
    private final ValidateSmsCallbacks validateSmsCallbacks;
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
    }

    public void process(Optional<OAuthToken> authToken, Message msg) {
        this.authToken = authToken;
        String token = (String) msg.obj;

        try {
            Response response = queryServer(token);
            parseResponse(response);
        } catch (Exception e) {
            validateSmsCallbacks.notifyError(new GenericServerError());
        }
    }

    Response queryServer(String token) throws IOException, JSONException {
        PinRequestDirect request = getRequest(token);

        request.setRetryPolicy(null);
        request.setOkHttpClient(new OkHttpClient());

        return request.loadDataFromNetwork();
    }

    private PinRequestDirect getRequest(String token) {
        return PinRequestDirect.builder()
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

    void parseResponse(Response response) {
        int code = response.code();
        switch (code) {
            case OK_200:
                validateSmsCallbacks.notifySuccess();
                break;
            case BAD_REQUEST_400:
            case FORBIDDEN_403:
                // TODO validate error, error invalid input
                validateSmsCallbacks.notifyError(new RequestValidationError());
                break;
            case NOT_FOUND_404: // TODO
                // TODO notify user details callback about
                // TODO validate error
                validateSmsCallbacks.notifyError(new TokenNotFound());
                break;
            default:
                validateSmsCallbacks.notifyError(new GenericServerError());
        }
    }
}
