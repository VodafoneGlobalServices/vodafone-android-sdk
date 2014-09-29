package com.vodafone.global.sdk.http.worker;

import android.content.Context;
import android.net.Uri;
import android.os.Message;
import com.google.common.base.Optional;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;
import com.vodafone.global.sdk.*;
import com.vodafone.global.sdk.http.HttpCode;
import com.vodafone.global.sdk.http.oauth.OAuthToken;
import com.vodafone.global.sdk.http.parser.Parsers;
import com.vodafone.global.sdk.http.resolve.ResolveGetRequestDirect;
import com.vodafone.global.sdk.http.resolve.UserDetailsDTO;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Set;

import static com.vodafone.global.sdk.MessageType.AUTHENTICATE;
import static com.vodafone.global.sdk.MessageType.CHECK_STATUS;
import static com.vodafone.global.sdk.http.HttpCode.*;

public class CheckStatusProcessor extends RequestProcessor {
    private String backendAppKey;
    private Optional<OAuthToken> authToken;
    private RequestBuilderProvider requestBuilderProvider;
    private UserDetailsDTO userDetailsDto;

    public CheckStatusProcessor(Context context, Worker worker, Settings settings, String backendAppKey, Set<ResolutionCallback> resolutionCallbacks, RequestBuilderProvider requestBuilderProvider) {
        super(context, worker, settings, resolutionCallbacks);
        this.backendAppKey = backendAppKey;
        this.requestBuilderProvider = requestBuilderProvider;
    }

    @Override
    public void process(Optional<OAuthToken> authToken, Message msg) {
        this.authToken = authToken;
        userDetailsDto = (UserDetailsDTO) msg.obj;

        try {
            Response response = queryServer();
            parseResponse(worker, response);
        } catch (IOException e) {
            notifyError(new GenericServerError());
        } catch (JSONException e) {
            notifyError(new GenericServerError());
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

    void parseResponse(Worker worker, Response response) throws IOException, JSONException {
        int code = response.code();
        switch (code) {
            case OK_200:
                notifyUserDetailUpdate(Parsers.resolutionCompleted(response));
                break;
            case FOUND_302:
                String location = response.header("Location");
                if (requiresSmsValidation(location)) {
                    if (canReadSMS()) {
                        generatePin(extractToken(location));
                    } else {
                        validationRequired(extractToken(location));
                    }
                } else {
                    int retryAfter = Integer.valueOf(response.header("Retry-After", "500"));
                    Message message = worker.createMessage(CHECK_STATUS, userDetailsDto);
                    worker.sendMessageDelayed(message, retryAfter);
                }
                break;
            case HttpCode.NOT_MODIFIED_304:
                UserDetailsDTO redirectDetails = Parsers.updateRetryAfter(userDetailsDto, response);
                Message message = worker.createMessage(CHECK_STATUS, redirectDetails);
                worker.sendMessageDelayed(message, redirectDetails.retryAfter);
                break;
            case BAD_REQUEST_400:
                resolutionFailed();
                break;
            case UNAUTHORIZED_401: // TODO 401 doesn't appear in documentation, it either should be removed or documented
                notifyError(new RequestNotAuthorized());
                break;
            case FORBIDDEN_403:
                String body = response.body().string();
                if (!body.isEmpty()) {
                    JSONObject json = new JSONObject(response.body().string());
                    String id = json.getString("id");
                    if (id.equals("POL0002")) {
                        worker.sendMessage(worker.createMessage(AUTHENTICATE));
                        worker.sendMessage(worker.createMessage(CHECK_STATUS, userDetailsDto));
                    }
                } else {
                    notifyError(new GenericServerError());
                }
                break;
            case NOT_FOUND_404:
                super.resolutionFailed();
                break;
            default:
                notifyError(new GenericServerError());
        }
    }
}
