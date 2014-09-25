package com.vodafone.global.sdk.http.worker;

import android.content.Context;
import android.net.Uri;
import android.os.Message;

import com.google.common.base.Optional;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;
import com.vodafone.global.sdk.GenericServerError;
import com.vodafone.global.sdk.IMSI;
import com.vodafone.global.sdk.InternalSdkError;
import com.vodafone.global.sdk.InvalidMsisdn;
import com.vodafone.global.sdk.MSISDN;
import com.vodafone.global.sdk.ResolutionStatus;
import com.vodafone.global.sdk.Settings;
import com.vodafone.global.sdk.UserDetailsCallback;
import com.vodafone.global.sdk.UserDetailsRequestParameters;
import com.vodafone.global.sdk.Utils;
import com.vodafone.global.sdk.VodafoneException;
import com.vodafone.global.sdk.VodafoneManager.MESSAGE_ID;
import com.vodafone.global.sdk.http.oauth.OAuthToken;
import com.vodafone.global.sdk.http.parser.Parsers;
import com.vodafone.global.sdk.http.resolve.ResolvePostRequestDirect;
import com.vodafone.global.sdk.http.resolve.UserDetailsDTO;

import org.json.JSONException;

import java.io.IOException;
import java.util.Set;

import static com.vodafone.global.sdk.http.HttpCode.BAD_REQUEST_400;
import static com.vodafone.global.sdk.http.HttpCode.CREATED_201;
import static com.vodafone.global.sdk.http.HttpCode.FORBIDDEN_403;
import static com.vodafone.global.sdk.http.HttpCode.FOUND_302;
import static com.vodafone.global.sdk.http.HttpCode.NOT_FOUND_404;
import static com.vodafone.global.sdk.http.HttpCode.UNAUTHORIZED_401;

public class ResolveUserProcessor extends RequestProcessor {
    private String backendAppKey;
    private IMSI imsi;
    private Optional<OAuthToken> authToken;

    public ResolveUserProcessor(Context context, Worker worker, Settings settings, String backendAppKey, IMSI imsi, Set<UserDetailsCallback> userDetailsCallbacks) {
        super(context, worker, settings, userDetailsCallbacks);
        this.backendAppKey = backendAppKey;
        this.imsi = imsi;
    }

    @Override
    public void process(Optional<OAuthToken> authToken, Message msg) {
        this.authToken = authToken;
        if (!authToken.isPresent()) {
            //Authenticate, then start again user retrieval
            worker.sendMessage(worker.createMessage(MESSAGE_ID.AUTHENTICATE.ordinal()));
            worker.sendMessage(worker.createMessage(msg));
        } else {
            try {
                Response response = queryServer((UserDetailsRequestParameters) msg.obj);
                parseResponse(worker, response);
            } catch (VodafoneException e) {
                notifyError(e);
            } catch (IOException e) {
                notifyError(new GenericServerError());
            } catch (JSONException e) {
                notifyError(new GenericServerError());
            }
        }
    }

    private Response queryServer(UserDetailsRequestParameters details) throws IOException, JSONException, VodafoneException {
        String androidId = Utils.getAndroidId(context);
        MSISDN msisdn = details.getMSISDN();
        String market = msisdn.marketCode();
        String url = getUrl(context, msisdn);

        ResolvePostRequestDirect request = ResolvePostRequestDirect.builder()
                .url(url)
                .accessToken(authToken.get().accessToken)
                .androidId(androidId)
                .mobileCountryCode(Utils.getMCC(context))
                .sdkId(settings.sdkId)
                .backendAppKey(backendAppKey)
                .imsi(imsi)
                .smsValidation(details.smsValidation())
                .msisdn(msisdn.get())
                .market(market)
                .build();
        request.setRetryPolicy(null);
        request.setOkHttpClient(new OkHttpClient());
        return request.loadDataFromNetwork();
    }

    private void parseResponse(Worker worker, Response response) {
        int code = response.code();
        try {
            switch (code) {
                case CREATED_201:
                    notifyUserDetailUpdate(Parsers.parseUserDetails(response).userDetails);
                    break;
                case NOT_FOUND_404:
                    //ERROR possibly to proceed with MSISDN OR OTP
                    // TODO add notifyUserDetailUpdate();
                    break;
                case FOUND_302: {
                    UserDetailsDTO redirectDetails = Parsers.parseRedirectDetails(response);
                    notifyUserDetailUpdate(redirectDetails.userDetails);
                    if (redirectDetails.userDetails.status != ResolutionStatus.VALIDATION_REQUIRED) {
                        worker.sendMessageDelayed(worker.createMessage(MESSAGE_ID.CHECK_STATUS.ordinal(), redirectDetails), redirectDetails.retryAfter);
                    }
                    // TODO call generatePin when we can intercept sms
                }
                break;
                case BAD_REQUEST_400:
                    //ERROR bad request - internal SDK error
                    notifyError(new InternalSdkError());
                    break;
                case UNAUTHORIZED_401:
                    //ERROR generic server error
                    notifyError(new GenericServerError());
                    break;
                case FORBIDDEN_403:
                    if (!response.body().string().isEmpty() && Utils.isHasTimedOut(authToken.get().expirationTime)) {
                        worker.sendMessage(worker.createMessage(MESSAGE_ID.AUTHENTICATE.ordinal()));
                        worker.sendMessage(worker.createMessage(MESSAGE_ID.RETRIEVE_USER_DETAILS.ordinal()));
                    } else {
                        //ERROR other error
                        notifyError(new GenericServerError());
                    }
                    break;
                default:
                    //ERROR generic server error
                    notifyError(new GenericServerError());
            }
        } catch (JSONException e) {
            notifyError(new GenericServerError());
        } catch (IOException e) {
            notifyError(new GenericServerError());
        }
    }

    private String getUrl(Context context, MSISDN msisdn) throws VodafoneException {
        Uri.Builder builder = new Uri.Builder();
        Uri uri;
        String marketCode = msisdn.marketCode();

        if (msisdn.isValid() && !marketCode.isEmpty()) {
            uri = builder.scheme(settings.apix.protocol)
                    .authority(settings.apix.host)
                    .path(settings.apix.path)
                    .appendQueryParameter("backendId", backendAppKey).build();
        } else if (Utils.isDataOverWiFi(context) && imsi.isPresent()) {
            uri = builder.scheme(settings.apix.protocol)
                    .authority(settings.apix.host)
                    .path(settings.apix.path)
                    .appendQueryParameter("backendId", backendAppKey).build();
        } else if (Utils.isDataOverMobileNetwork(context)) {
            uri = builder.scheme(settings.hap.protocol)
                    .authority(settings.hap.host)
                    .path(settings.hap.path)
                    .appendQueryParameter("backendId", backendAppKey).build();
        } else {
            // TODO some reasonable error handling/code
            throw new InvalidMsisdn();
        }
        return uri.toString();
    }
}
