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
import com.vodafone.global.sdk.UserDetailsRequestParameters;
import com.vodafone.global.sdk.Utils;
import com.vodafone.global.sdk.VodafoneException;
import com.vodafone.global.sdk.VodafoneManager;
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
    private String appId;
    private SimSerialNumber iccid;
    private Optional<OAuthToken> authToken;

    public ResolveUserProcessor(Context context, Settings settings, String appId, SimSerialNumber iccid, Set<UserDetailsCallback> userDetailsCallbacks) {
        super(context, settings, userDetailsCallbacks);
        this.appId = appId;
        this.iccid = iccid;
    }

    public void parseResponse(Worker worker, Response response) {
        int code = response.code();
        try {
            switch (code) {
                case CREATED_201:
                    notifyUserDetailUpdate(Parsers.parseUserDetails(response));
                    break;
                case NOT_FOUND_404:
                    //ERROR possibly to proceed with MSISDN OR OTP
                    //TODO add notifyUserDetailUpdate();
                    break;
                case FOUND_302: {
                    UserDetailsDTO redirectDetails = Parsers.parseRedirectDetails(response);
                    if (redirectDetails.userDetails.validationRequired) {
                        notifyUserDetailUpdate(redirectDetails);
                    } else {
                        notifyUserDetailUpdate(redirectDetails);
                        worker.sendMessage(worker.createMessage(VodafoneManager.MESSAGE_ID.REDIRECT.ordinal(), redirectDetails));
                    }
                }
                break;
                case BAD_REQUEST_400:
                    //ERROR bad request - internal SDK error
                    notifyError(new VodafoneException(VodafoneException.EXCEPTION_TYPE.INTERNAL_SDK_ERROR));
                    break;
                case UNAUTHORIZED_401:
                    //ERROR generic server error
                    notifyError(new VodafoneException(VodafoneException.EXCEPTION_TYPE.GENERIC_SERVER_ERROR));
                    break;
                case FORBIDDEN_403:
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

    public Response queryServer(UserDetailsRequestParameters details) throws IOException, JSONException, VodafoneException {
        String androidId = Utils.getAndroidId(context);
        String msisdn = details.getMSISDN();
        String market = getMarketCode(msisdn);
        String url = getUrl(context, msisdn);

        ResolvePostRequestDirect request = ResolvePostRequestDirect.builder()
                .url(url)
                .accessToken(authToken.get().accessToken)
                .androidId(androidId)
                .mobileCountryCode(Utils.getMCC(context))
                .sdkId(settings.sdkId)
                .appId(appId)
                .imsi(iccid)
                .smsValidation(details.smsValidation())
                .msisdn(msisdn)
                .market(market)
                .build();
        request.setRetryPolicy(null);
        request.setOkHttpClient(new OkHttpClient());
        return request.loadDataFromNetwork();
    }

    @Override
    public void process(Worker worker, Optional<OAuthToken> authToken, Message msg) {
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
                notifyError(new VodafoneException(VodafoneException.EXCEPTION_TYPE.GENERIC_SERVER_ERROR));
            } catch (JSONException e) {
                notifyError(new VodafoneException(VodafoneException.EXCEPTION_TYPE.GENERIC_SERVER_ERROR));
            }
        }
    }

    private String getMarketCode(String msisdn) {
        if (msisdn.isEmpty()) {
            return "";
        } else {
            //TODO get country code
            return "DE";
        }
    }

    private boolean isMsisdnValid(String msisdn) {
        //TODO add country code check
        return (msisdn.matches(settings.msisdnValidationRegex) || msisdn.isEmpty());
    }

    private String getUrl(Context context, String msisdn) throws VodafoneException {
        Uri.Builder builder = new Uri.Builder();
        Uri uri;
        String marketCode = getMarketCode(msisdn);

        if (isMsisdnValid(msisdn) && !marketCode.isEmpty()) {
            uri = builder.scheme(settings.apix.protocol).authority(settings.apix.host).path(settings.apix.path).appendQueryParameter("backendId", appId).build();
        } else if (Utils.isDataOverWiFi(context) && iccid.isPresent()) {
            uri = builder.scheme(settings.apix.protocol).authority(settings.apix.host).path(settings.apix.path).appendQueryParameter("backendId", appId).build();
        } else if (Utils.isDataOverMobileNetwork(context)) {
            uri = builder.scheme(settings.hap.protocol).authority(settings.hap.host).path(settings.hap.path).appendQueryParameter("backendId", appId).build();
        } else {
            throw new VodafoneException(VodafoneException.EXCEPTION_TYPE.INVALID_MSISDN); //TODO some reasonable error handling/code
        }
        return uri.toString();
    }
}
