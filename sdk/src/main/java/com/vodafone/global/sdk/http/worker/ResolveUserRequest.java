package com.vodafone.global.sdk.http.worker;

import android.content.Context;
import android.net.Uri;
import android.os.Message;
import android.util.Log;

import com.google.common.base.Optional;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;
import com.vodafone.global.sdk.Settings;
import com.vodafone.global.sdk.SimSerialNumber;
import com.vodafone.global.sdk.UserDetailsCallback;
import com.vodafone.global.sdk.UserDetailsRequestParameters;
import com.vodafone.global.sdk.Utils;
import com.vodafone.global.sdk.VodafoneException;
import com.vodafone.global.sdk.VodafoneManager;
import com.vodafone.global.sdk.VodafoneManager.*;
import com.vodafone.global.sdk.http.oauth.OAuthToken;
import com.vodafone.global.sdk.http.parser.Parsers;
import com.vodafone.global.sdk.http.resolve.ResolvePostRequestDirect;
import com.vodafone.global.sdk.http.resolve.UserDetailsDTO;

import org.json.JSONException;
import java.io.IOException;
import java.util.Set;

/**
 * Created by bamik on 2014-09-10.
 */
public class ResolveUserRequest extends ServerRequest {
    private static final String TAG = ResolveUserRequest.class.getSimpleName();
    private String appId;
    private SimSerialNumber iccid;
    private Optional<OAuthToken> authToken;

    public ResolveUserRequest(Context context, Settings settings, String appId, SimSerialNumber iccid, Set<UserDetailsCallback> userDetailsCallbacks) {
        super(context, settings, userDetailsCallbacks);
        this.appId = appId;
        this.iccid = iccid;
    }

    public void parseResponse(Worker worker, Response response, UserDetailsDTO userDetails) {
        int code = response.code();
        try {
            switch (code) {
                case 200: //TODO remove on working test environment
                case 201:
                    notifyUserDetailUpdate(Parsers.parseUserDetails(response));
                    break;
                case 404:
                    //ERROR possibly to proceed with MSISDN OR OTP
                    //TODO add notifyUserDetailUpdate();
                    break;
                case 302: {
                    UserDetailsDTO redirectDetails = Parsers.parseRedirectDetails(response);
                    if (redirectDetails.userDetails.validationRequired) {
                        notifyUserDetailUpdate(Parsers.parseUserDetails(response));
                    } else {
                        notifyUserDetailUpdate(Parsers.parseUserDetails(response));
                        worker.sendMessage(worker.createMessage(VodafoneManager.MESSAGE_ID.REDIRECT.ordinal(), redirectDetails));
                    }
                }
                break;
                case 400:
                    //ERROR bad request - internal SDK error
                    notifyError(new VodafoneException(VodafoneException.EXCEPTION_TYPE.INTERNAL_SDK_ERROR));
                    break;
                case 401:
                    //ERROR generic server error
                    notifyError(new VodafoneException(VodafoneException.EXCEPTION_TYPE.GENERIC_SERVER_ERROR));
                    break;
                case 403:
                    if (!response.body().string().isEmpty() && Utils.isHasTimedOut(authToken.get().expirationTime)) {
                        worker.sendMessage(worker.createMessage(MESSAGE_ID.AUTHENTICATE.ordinal()));
                        worker.sendMessage(worker.createMessage(MESSAGE_ID.RETRIEVE_USER_DETAILS.ordinal()));
                    } else {
                        //ERROR other error
                        notifyError(new VodafoneException(VodafoneException.EXCEPTION_TYPE.GENERIC_SERVER_ERROR));
                    }
                    break;
                default:
                    //ERROR generic server error
                    notifyError(new VodafoneException(VodafoneException.EXCEPTION_TYPE.GENERIC_SERVER_ERROR));
            }
        } catch (JSONException e) {
            notifyError(new VodafoneException(VodafoneException.EXCEPTION_TYPE.GENERIC_SERVER_ERROR));
        } catch (IOException e) {
            notifyError(new VodafoneException(VodafoneException.EXCEPTION_TYPE.GENERIC_SERVER_ERROR));
        }
    }

    public Response queryServer(UserDetailsRequestParameters details) throws IOException, JSONException {
        String androidId = Utils.getAndroidId(context);
        Uri.Builder builder = new Uri.Builder();
        Uri uri = builder.scheme(settings.resolveOverWiFi.protocol).authority(settings.resolveOverWiFi.host).path(settings.resolveOverWiFi.path).appendQueryParameter("backendId", appId).build();
        ResolvePostRequestDirect request = ResolvePostRequestDirect.builder()
                .url(uri.toString())
                .accessToken(authToken.get().accessToken)
                .androidId(androidId)
                .mobileCountryCode(Utils.getMCC(context))
                .sdkId(settings.sdkId)
                .appId(appId)
                .imsi(iccid)
                .smsValidation(details.smsValidation())
                .build();
        request.setRetryPolicy(null);
        request.setOkHttpClient(new OkHttpClient());
        return request.loadDataFromNetwork();
    }

    @Override
    public void process(Worker worker, Optional<OAuthToken> authToken, Message msg) {
        if (!authToken.isPresent()) {
            //Authenticate, then start again user retrieval
            worker.sendMessage(worker.createMessage(MESSAGE_ID.AUTHENTICATE.ordinal()));
            worker.sendMessage(worker.createMessage(msg));
        } else {
            try {
                this.authToken = authToken;
                parseResponse(worker, queryServer((UserDetailsRequestParameters) msg.obj), null);
            } catch (Exception e) { //TODO add detailed exception handling

            }
        }
    }
}
