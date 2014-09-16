package com.vodafone.global.sdk.http.worker;

import android.content.Context;
import android.net.Uri;
import android.os.Message;

import com.google.common.base.Optional;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;
import com.vodafone.global.sdk.Settings;
import com.vodafone.global.sdk.Utils;
import com.vodafone.global.sdk.ValidateSmsCallback;
import com.vodafone.global.sdk.VodafoneException;
import com.vodafone.global.sdk.http.oauth.OAuthToken;
import com.vodafone.global.sdk.http.sms.PinRequestDirect;

import org.json.JSONException;

import java.io.IOException;
import java.util.Set;

public class GeneratePinProcessor extends PinProcessor {
    private String appId;
    private Optional<OAuthToken> authToken;

    public GeneratePinProcessor(Context context, Settings settings, String appId, Set<ValidateSmsCallback> validateSmsCallbacks) {
        super(context, settings, validateSmsCallbacks);
        this.appId = appId;
    }

    void parseResponse(Response response) {
        int code = response.code();
        switch (code) {
            case 200: //TODO update listeners properly
                notifySuccess();
                break;
            case 400:
                notifyError(new VodafoneException(VodafoneException.EXCEPTION_TYPE.REQUEST_VALIDATION_ERROR));
                break;
            case 401:
            case 403:
                notifyError(new VodafoneException(VodafoneException.EXCEPTION_TYPE.TOKEN_NOT_FOUND));
                break;
            case 404:
                notifyError(new VodafoneException(VodafoneException.EXCEPTION_TYPE.TOKEN_NOT_FOUND));
                break;
            default: //5xx and other critical errors
                notifyError(new VodafoneException(VodafoneException.EXCEPTION_TYPE.GENERIC_SERVER_ERROR));
        }
    }

    Response queryServer(String token) throws IOException, JSONException {
        String androidId = Utils.getAndroidId(context);

        Uri.Builder builder = new Uri.Builder();
        Uri uri = builder.scheme(settings.apix.protocol)
                .authority(settings.apix.host)
                .path(settings.apix.path)
                .appendPath(token)
                .appendPath("pins")
                .appendQueryParameter("backendId", appId).build();

        PinRequestDirect request = PinRequestDirect.builder()
                .url(uri.toString())
                .accessToken(authToken.get().accessToken)
                .androidId(androidId)
                .mobileCountryCode(Utils.getMCC(context))
                .sdkId(settings.sdkId)
                .appId(appId)
                .build();

        request.setRetryPolicy(null);
        request.setOkHttpClient(new OkHttpClient());
        return request.loadDataFromNetwork();
    }

    @Override
    public void process(Worker worker, Optional<OAuthToken> authToken, Message msg) {
        String token = (String) msg.obj;

        try {
            this.authToken = authToken;
            parseResponse(queryServer(token));
        } catch (Exception e) {
            notifyError(new VodafoneException(VodafoneException.EXCEPTION_TYPE.GENERIC_SERVER_ERROR));
        }
    }
}
