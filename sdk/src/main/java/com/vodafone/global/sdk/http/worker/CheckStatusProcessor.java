package com.vodafone.global.sdk.http.worker;

import android.content.Context;
import android.net.Uri;
import android.os.Message;

import com.google.common.base.Optional;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;
import com.vodafone.global.sdk.Settings;
import com.vodafone.global.sdk.SimSerialNumber;
import com.vodafone.global.sdk.UserDetailsCallback;
import com.vodafone.global.sdk.Utils;
import com.vodafone.global.sdk.VodafoneException;
import com.vodafone.global.sdk.VodafoneManager.MESSAGE_ID;
import com.vodafone.global.sdk.http.HttpCode;
import com.vodafone.global.sdk.http.oauth.OAuthToken;
import com.vodafone.global.sdk.http.parser.Parsers;
import com.vodafone.global.sdk.http.resolve.ResolveGetRequestDirect;
import com.vodafone.global.sdk.http.resolve.UserDetailsDTO;

import org.json.JSONException;

import java.io.IOException;
import java.util.Set;

import static com.vodafone.global.sdk.http.HttpCode.BAD_REQUEST_400;
import static com.vodafone.global.sdk.http.HttpCode.FORBIDDEN_403;
import static com.vodafone.global.sdk.http.HttpCode.FOUND_302;
import static com.vodafone.global.sdk.http.HttpCode.NOT_FOUND_404;
import static com.vodafone.global.sdk.http.HttpCode.OK_200;
import static com.vodafone.global.sdk.http.HttpCode.UNAUTHORIZED_401;

public class CheckStatusProcessor extends RequestProcessor {
    private String backendAppKey;
    private SimSerialNumber iccid;
    private Optional<OAuthToken> authToken;

    public CheckStatusProcessor(Context context, Settings settings, String backendAppKey, SimSerialNumber iccid, Set<UserDetailsCallback> userDetailsCallbacks) {
        super(context, settings, userDetailsCallbacks);
        this.backendAppKey = backendAppKey;
        this.iccid = iccid;
    }

    void parseResponse(Worker worker, Response response, UserDetailsDTO oldRedirectDetails) {
        int code = response.code();
        try {
            switch (code) {
                case OK_200:
                    notifyUserDetailUpdate(Parsers.parseUserDetails(response));
                case FOUND_302: {
                    UserDetailsDTO redirectDetails  = Parsers.parseRedirectDetails(response);
                    if (oldRedirectDetails.userDetails.validationRequired) {
                        notifyUserDetailUpdate(redirectDetails);
                    } else {
                        worker.sendMessageDelayed(worker.createMessage(MESSAGE_ID.CHECK_STATUS.ordinal(), redirectDetails), redirectDetails.retryAfter);
                    }
                }
                break;
                case HttpCode.NOT_MODIFIED_304: {
                    UserDetailsDTO redirectDetails = Parsers.updateRetryAfter(oldRedirectDetails, response);
                    worker.sendMessageDelayed(worker.createMessage(MESSAGE_ID.CHECK_STATUS.ordinal(), redirectDetails), redirectDetails.retryAfter);
                }
                break;
                case BAD_REQUEST_400:
                    //ERROR bad request - internal SDK error
                    notifyError(new VodafoneException(VodafoneException.EXCEPTION_TYPE.INTERNAL_SDK_ERROR));
                    break;
                case UNAUTHORIZED_401:
                    //ERROR Unauthorized access
                    notifyError(new VodafoneException(VodafoneException.EXCEPTION_TYPE.REQUEST_NOT_AUTHORIZED));
                    break;
                case FORBIDDEN_403:
                    if (!response.body().string().isEmpty() && Utils.isHasTimedOut(authToken.get().expirationTime)) {
                        worker.sendMessage(worker.createMessage(MESSAGE_ID.AUTHENTICATE.ordinal()));
                        worker.sendMessage(worker.createMessage(MESSAGE_ID.CHECK_STATUS.ordinal(), oldRedirectDetails));
                    } else {
                        notifyError(new VodafoneException(VodafoneException.EXCEPTION_TYPE.GENERIC_SERVER_ERROR));
                    }
                    break;
                case NOT_FOUND_404:
                    //ERROR repeat from get user status
                    worker.sendMessage(worker.createMessage(MESSAGE_ID.RETRIEVE_USER_DETAILS.ordinal()));
                    break;
                default: //5xx and other critical errors
                    notifyError(new VodafoneException(VodafoneException.EXCEPTION_TYPE.GENERIC_SERVER_ERROR));
            }
        } catch (JSONException e) {
            notifyError(new VodafoneException(VodafoneException.EXCEPTION_TYPE.GENERIC_SERVER_ERROR));
        } catch (IOException e) {
            notifyError(new VodafoneException(VodafoneException.EXCEPTION_TYPE.GENERIC_SERVER_ERROR));
        }
    }

    Response queryServer(UserDetailsDTO details) throws IOException, JSONException {
        String androidId = Utils.getAndroidId(context);
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
                .androidId(androidId)
                .mobileCountryCode(Utils.getMCC(context))
                .sdkId(settings.sdkId)
                .backendAppKey(backendAppKey)
                .etag(details.etag)
                .build();
        request.setRetryPolicy(null);
        request.setOkHttpClient(new OkHttpClient());

        return request.loadDataFromNetwork();
    }

    @Override
    public void process(Worker worker, Optional<OAuthToken> authToken, Message msg) {
        UserDetailsDTO redirectDetails = (UserDetailsDTO) msg.obj;

        try {
            this.authToken = authToken;
            parseResponse(worker, queryServer(redirectDetails), redirectDetails);
        } catch (Exception e) {

        }
    }
}
