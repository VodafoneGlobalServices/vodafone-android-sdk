package com.vodafone.global.sdk.http.worker;

import android.content.Context;
import android.net.Uri;
import android.os.Message;

import com.google.common.base.Optional;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;
import com.vodafone.global.sdk.GenericServerError;
import com.vodafone.global.sdk.RequestValidationError;
import com.vodafone.global.sdk.Settings;
import com.vodafone.global.sdk.TokenNotFound;
import com.vodafone.global.sdk.Utils;
import com.vodafone.global.sdk.ValidateSmsCallback;
import com.vodafone.global.sdk.http.oauth.OAuthToken;
import com.vodafone.global.sdk.http.sms.PinRequestDirect;

import org.json.JSONException;

import java.io.IOException;
import java.util.Set;

import static com.vodafone.global.sdk.http.HttpCode.BAD_REQUEST_400;
import static com.vodafone.global.sdk.http.HttpCode.FORBIDDEN_403;
import static com.vodafone.global.sdk.http.HttpCode.NOT_FOUND_404;
import static com.vodafone.global.sdk.http.HttpCode.OK_200;
import static com.vodafone.global.sdk.http.HttpCode.UNAUTHORIZED_401;

public class GeneratePinProcessor extends PinProcessor {
    private String backendAppKey;
    private Optional<OAuthToken> authToken;

    public GeneratePinProcessor(Context context, Settings settings, String backendAppKey, Set<ValidateSmsCallback> validateSmsCallbacks) {
        super(context, settings, validateSmsCallbacks);
        this.backendAppKey = backendAppKey;
    }

    void parseResponse(Response response) {
        int code = response.code();
        switch (code) {
            case OK_200: //TODO update listeners properly
                notifySuccess();
                break;
            case BAD_REQUEST_400:
                notifyError(new RequestValidationError());
                break;
            case UNAUTHORIZED_401:
            case FORBIDDEN_403:
                notifyError(new TokenNotFound());
                break;
            case NOT_FOUND_404:
                notifyError(new TokenNotFound());
                break;
            default: //5xx and other critical errors
                notifyError(new GenericServerError());
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
                .appendQueryParameter("backendId", backendAppKey).build();

        PinRequestDirect request = PinRequestDirect.builder()
                .url(uri.toString())
                .accessToken(authToken.get().accessToken)
                .androidId(androidId)
                .mobileCountryCode(Utils.getMCC(context))
                .sdkId(settings.sdkId)
                .backendAppKey(backendAppKey)
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
            notifyError(new GenericServerError());
        }
    }
}
