package com.vodafone.global.sdk.http.worker;

import android.content.Context;
import android.net.Uri;
import android.os.Message;
import com.google.common.base.Optional;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;
import com.vodafone.global.sdk.GenericServerError;
import com.vodafone.global.sdk.RequestBuilderProvider;
import com.vodafone.global.sdk.ResolveCallbacks;
import com.vodafone.global.sdk.Settings;
import com.vodafone.global.sdk.http.oauth.OAuthToken;
import com.vodafone.global.sdk.http.resolve.ResolveGetRequestDirect;
import com.vodafone.global.sdk.http.resolve.UserDetailsDTO;
import org.json.JSONException;

import java.io.IOException;

public class CheckStatusProcessor extends RequestProcessor {
    private final CheckStatusParser parser;
    private String backendAppKey;
    private Optional<OAuthToken> authToken;
    private RequestBuilderProvider requestBuilderProvider;
    private UserDetailsDTO userDetailsDto;

    public CheckStatusProcessor(Context context, Worker worker, Settings settings, String backendAppKey, ResolveCallbacks resolveCallbacks, RequestBuilderProvider requestBuilderProvider) {
        super(context, worker, settings, resolveCallbacks);
        this.backendAppKey = backendAppKey;
        this.requestBuilderProvider = requestBuilderProvider;
        parser = new CheckStatusParser(worker, context, resolveCallbacks);
    }

    @Override
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
        ResolveGetRequestDirect request = getRequest();

        request.setRetryPolicy(null);
        request.setOkHttpClient(new OkHttpClient());

        return request.loadDataFromNetwork();
    }

    private ResolveGetRequestDirect getRequest() {
        ResolveGetRequestDirect.Builder requestBuilder = ResolveGetRequestDirect.builder()
                .url(getUrl())
                .accessToken(authToken.get().accessToken)
                .requestBuilderProvider(requestBuilderProvider);
        if (!userDetailsDto.etag.isEmpty())
            requestBuilder.etag(userDetailsDto.etag);
        return requestBuilder.build();
    }

    private String getUrl() {
        return new Uri.Builder().scheme(settings.apix.protocol)
                .authority(settings.apix.host)
                .path(settings.apix.path)
                .appendPath(userDetailsDto.userDetails.token)
                .appendQueryParameter("backendId", backendAppKey)
                .build().toString();
    }
}
