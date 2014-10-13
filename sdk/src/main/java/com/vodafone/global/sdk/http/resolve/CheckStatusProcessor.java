package com.vodafone.global.sdk.http.resolve;

import android.content.Context;
import android.net.Uri;
import android.os.Message;
import com.google.common.base.Optional;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;
import com.vodafone.global.sdk.http.GenericServerError;
import com.vodafone.global.sdk.RequestBuilderProvider;
import com.vodafone.global.sdk.ResolveCallbacks;
import com.vodafone.global.sdk.Settings;
import com.vodafone.global.sdk.http.oauth.OAuthToken;
import com.vodafone.global.sdk.Worker;
import com.vodafone.global.sdk.logging.Logger;
import org.json.JSONException;

import java.io.IOException;

public class CheckStatusProcessor {
    protected final Settings settings;
    protected final ResolveCallbacks resolveCallbacks;
    private final CheckStatusParser parser;
    private String backendAppKey;
    private Optional<OAuthToken> authToken;
    private RequestBuilderProvider requestBuilderProvider;
    private final Logger logger;
    private UserDetailsDTO userDetailsDto;

    public CheckStatusProcessor(
            Context context,
            Worker worker,
            Settings settings,
            String backendAppKey,
            ResolveCallbacks resolveCallbacks,
            RequestBuilderProvider requestBuilderProvider,
            Logger logger
    ) {
        this.settings = settings;
        this.resolveCallbacks = resolveCallbacks;
        this.backendAppKey = backendAppKey;
        this.requestBuilderProvider = requestBuilderProvider;
        this.logger = logger;
        parser = new CheckStatusParser(worker, context, resolveCallbacks);
    }

    public void process(Optional<OAuthToken> authToken, Message msg) {
        this.authToken = authToken;
        userDetailsDto = (UserDetailsDTO) msg.obj;

        try {
            Response response = queryServer();
            parser.parseResponse(response, userDetailsDto);
        } catch (IOException e) {
            resolveCallbacks.notifyError(new GenericServerError());
        } catch (JSONException e) {
            resolveCallbacks.notifyError(new GenericServerError());
        }
    }

    Response queryServer() throws IOException, JSONException {
        ResolveGetRequest request = getRequest();

        request.setRetryPolicy(null);
        request.setOkHttpClient(new OkHttpClient());

        return request.loadDataFromNetwork();
    }

    private ResolveGetRequest getRequest() {
        ResolveGetRequest.Builder requestBuilder = ResolveGetRequest.builder()
                .url(getUrl())
                .accessToken(authToken.get().accessToken)
                .requestBuilderProvider(requestBuilderProvider)
                .logger(logger);
        if (!userDetailsDto.etag.isPresent())
            requestBuilder.etag(userDetailsDto.etag.get());
        return requestBuilder.build();
    }

    private String getUrl() {
        return new Uri.Builder().scheme(settings.apix.protocol)
                .authority(settings.apix.host)
                .path(settings.apix.path)
                .appendPath(userDetailsDto.userDetails.get().token)
                .appendQueryParameter("backendId", backendAppKey)
                .build().toString();
    }
}
