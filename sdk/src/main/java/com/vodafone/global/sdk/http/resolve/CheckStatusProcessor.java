package com.vodafone.global.sdk.http.resolve;

import android.content.Context;
import android.net.Uri;
import android.os.Message;
import com.google.common.base.Optional;
import com.squareup.okhttp.OkHttpClient;
import com.vodafone.global.sdk.RequestBuilderProvider;
import com.vodafone.global.sdk.ResolveCallbacks;
import com.vodafone.global.sdk.Settings;
import com.vodafone.global.sdk.Worker;
import com.vodafone.global.sdk.http.GenericServerError;
import com.vodafone.global.sdk.http.ResponseHolder;
import com.vodafone.global.sdk.http.oauth.OAuthToken;
import com.vodafone.global.sdk.logging.Logger;
import org.json.JSONException;

import java.io.IOException;

public class CheckStatusProcessor {
    private final OkHttpClient httpClient;
    protected final Settings settings;
    protected final ResolveCallbacks resolveCallbacks;
    private final CheckStatusParser parser;
    private String backendAppKey;
    private Optional<OAuthToken> authToken;
    private RequestBuilderProvider requestBuilderProvider;
    private final Logger logger;
    private CheckStatusParameters checkStatusParameters;

    public CheckStatusProcessor(
            Context context,
            OkHttpClient httpClient,
            Worker worker,
            Settings settings,
            String backendAppKey,
            ResolveCallbacks resolveCallbacks,
            RequestBuilderProvider requestBuilderProvider,
            Logger logger
    ) {
        this.httpClient = httpClient;
        this.settings = settings;
        this.resolveCallbacks = resolveCallbacks;
        this.backendAppKey = backendAppKey;
        this.requestBuilderProvider = requestBuilderProvider;
        this.logger = logger;
        parser = new CheckStatusParser(worker, context, resolveCallbacks);
    }

    public void process(Optional<OAuthToken> authToken, Message msg) {
        this.authToken = authToken;
        checkStatusParameters = (CheckStatusParameters) msg.obj;

        try {
            ResolveGetRequest request = getRequest();
            request.setOkHttpClient(httpClient);
            ResponseHolder response = request.loadDataFromNetwork();
            parser.parseResponse(response, checkStatusParameters);
        } catch (IOException e) {
            resolveCallbacks.notifyError(new GenericServerError(e));
        } catch (JSONException e) {
            resolveCallbacks.notifyError(new GenericServerError(e));
        }
    }

    private ResolveGetRequest getRequest() {
        return ResolveGetRequest.builder()
                .url(getUrl())
                .accessToken(authToken.get().accessToken)
                .requestBuilderProvider(requestBuilderProvider)
                .logger(logger)
                .etag(checkStatusParameters.etag)
                .build();
    }

    private String getUrl() {
        return new Uri.Builder()
                .scheme(settings.apix().protocol())
                .authority(settings.apix().host())
                .path(settings.apix().path())
                .appendPath(checkStatusParameters.tokenId.get())
                .appendQueryParameter("backendId", backendAppKey)
                .build()
                .toString();
    }
}
