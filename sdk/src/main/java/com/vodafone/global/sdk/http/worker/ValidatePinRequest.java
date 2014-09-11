package com.vodafone.global.sdk.http.worker;

import android.content.Context;
import android.os.Message;
import android.util.Log;

import com.google.common.base.Optional;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;
import com.vodafone.global.sdk.Settings;
import com.vodafone.global.sdk.SimSerialNumber;
import com.vodafone.global.sdk.UserDetails;
import com.vodafone.global.sdk.UserDetailsCallback;
import com.vodafone.global.sdk.ValidatePinParameters;
import com.vodafone.global.sdk.VodafoneException;
import com.vodafone.global.sdk.http.oauth.OAuthToken;
import com.vodafone.global.sdk.http.sms.PinRequestDirect;
import com.vodafone.global.sdk.http.sms.ValidatePinRequestDirect;

import org.json.JSONException;

import java.io.IOException;
import java.util.Set;

/**
 * Created by bamik on 2014-09-10.
 */
public class ValidatePinRequest extends ServerRequest {
    private static final String TAG = ValidatePinRequest.class.getSimpleName();
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private String appId;
    private SimSerialNumber iccid;
    private Optional<OAuthToken> authToken;

    public ValidatePinRequest(Context context, Settings settings, String appId, SimSerialNumber iccid, Set<UserDetailsCallback> userDetailsCallbacks) {
        super(context, settings, userDetailsCallbacks);
        this.appId = appId;
        this.iccid = iccid;
    }

    void parseResponse(Worker worker, Response response, ValidatePinParameters validatePinParameters) {
        int code = response.code();
        switch (code) {
            case 200: //TODO remove on working test environment
                break;
            case 302:
                break;
            case 304:
                break;
            case 400:
                break;
            case 401:
                break;
            case 403:
                break;
            case 404:
                break;
            default: //5xx and other critical errors
                notifyError(new VodafoneException(VodafoneException.EXCEPTION_TYPE.GENERIC_SERVER_ERROR));
        }
    }

    Response queryServer(ValidatePinParameters validatePinParameters) throws IOException, JSONException {
        // TODO update request
        ValidatePinRequestDirect request = ValidatePinRequestDirect.builder()
                .url(settings.validatePin.protocol + "://"
                        + settings.validatePin.host
                        + settings.validatePin.path
                        + "/" + validatePinParameters.getUserDetails().token)
                .build();

        request.setRetryPolicy(null);
        request.setOkHttpClient(new OkHttpClient());
        return request.loadDataFromNetwork();
    }

    @Override
    public void process(Worker worker, Optional<OAuthToken> authToken, Message msg) {
        ValidatePinParameters validatePinParameters = (ValidatePinParameters) msg.obj;

        try {
            this.authToken = authToken;
            parseResponse(worker, queryServer(validatePinParameters), validatePinParameters);
        } catch (Exception e) {

        }
    }
}
