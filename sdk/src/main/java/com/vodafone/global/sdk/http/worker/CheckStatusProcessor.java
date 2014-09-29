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

import java.io.IOException;
import java.util.Set;

import static com.vodafone.global.sdk.MessageType.*;
import static com.vodafone.global.sdk.http.HttpCode.*;

public class CheckStatusProcessor extends RequestProcessor {
    private String backendAppKey;
    private Optional<OAuthToken> authToken;
    private RequestBuilderProvider requestBuilderProvider;

    public CheckStatusProcessor(Context context, Worker worker, Settings settings, String backendAppKey, IMSI imsi, Set<ResolutionCallback> resolutionCallbacks, RequestBuilderProvider requestBuilderProvider) {
        super(context, worker, settings, resolutionCallbacks);
        this.backendAppKey = backendAppKey;
        this.requestBuilderProvider = requestBuilderProvider;
    }

    void parseResponse(Worker worker, Response response, UserDetailsDTO oldRedirectDetails) {
        int code = response.code();
        try {
            switch (code) {
                case OK_200:
                    notifyUserDetailUpdate(Parsers.parseUserDetails(response));
                case FOUND_302: {
                    UserDetailsDTO redirectDetails  = Parsers.parseRedirectDetails(response);
                    if (oldRedirectDetails.status == ResolutionStatus.VALIDATION_REQUIRED) {
                        notifyUserDetailUpdate(redirectDetails);
                    } else {
                        worker.sendMessageDelayed(worker.createMessage(CHECK_STATUS, redirectDetails), redirectDetails.retryAfter);
                    }
                }
                break;
                case HttpCode.NOT_MODIFIED_304: {
                    UserDetailsDTO redirectDetails = Parsers.updateRetryAfter(oldRedirectDetails, response);
                    worker.sendMessageDelayed(worker.createMessage(CHECK_STATUS, redirectDetails), redirectDetails.retryAfter);
                }
                break;
                case BAD_REQUEST_400:
                    //ERROR bad request - internal SDK error
                    notifyError(new BadRequest());
                    break;
                case UNAUTHORIZED_401:
                    //ERROR Unauthorized access
                    notifyError(new RequestNotAuthorized());
                    break;
                case FORBIDDEN_403:
                    if (!response.body().string().isEmpty() && Utils.isHasTimedOut(authToken.get().expirationTime)) {
                        worker.sendMessage(worker.createMessage(AUTHENTICATE));
                        worker.sendMessage(worker.createMessage(CHECK_STATUS, oldRedirectDetails));
                    } else {
                        notifyError(new GenericServerError());
                    }
                    break;
                case NOT_FOUND_404:
                    //ERROR repeat from get user status
                    worker.sendMessage(worker.createMessage(RETRIEVE_USER_DETAILS));
                    break;
                default: //5xx and other critical errors
                    notifyError(new GenericServerError());
            }
        } catch (JSONException e) {
            notifyError(new GenericServerError());
        } catch (IOException e) {
            notifyError(new GenericServerError());
        }
    }

    Response queryServer(UserDetailsDTO details) throws IOException, JSONException {
        Uri.Builder builder = new Uri.Builder();
        Uri uri = builder.scheme(settings.apix.protocol)
                .authority(settings.apix.host)
                .path(settings.apix.path)
                .appendPath(details.userDetails.token)
                .appendQueryParameter("backendId", backendAppKey)
                    .build();

        ResolveGetRequestDirect request = ResolveGetRequestDirect.builder()
                .url(uri.toString())
                .accessToken(authToken.get().accessToken)
                .requestBuilderProvider(requestBuilderProvider)
                .etag(details.etag)
                .build();
        request.setRetryPolicy(null);
        request.setOkHttpClient(new OkHttpClient());

        return request.loadDataFromNetwork();
    }

    @Override
    public void process(Optional<OAuthToken> authToken, Message msg) {
        UserDetailsDTO redirectDetails = (UserDetailsDTO) msg.obj;

        try {
            this.authToken = authToken;
            parseResponse(worker, queryServer(redirectDetails), redirectDetails);
        } catch (Exception e) {

        }
    }
}
