package com.vodafone.global.sdk.http.worker;

import android.content.Context;
import android.net.Uri;
import android.os.Message;

import com.google.common.base.Optional;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;
import com.vodafone.global.sdk.Settings;
import com.vodafone.global.sdk.UserDetailsCallback;
import com.vodafone.global.sdk.Utils;
import com.vodafone.global.sdk.ValidatePinParameters;
import com.vodafone.global.sdk.VodafoneException;
import com.vodafone.global.sdk.http.HttpCode;
import com.vodafone.global.sdk.http.oauth.OAuthToken;
import com.vodafone.global.sdk.http.parser.Parsers;
import com.vodafone.global.sdk.http.sms.ValidatePinRequestDirect;

import org.json.JSONException;

import java.io.IOException;
import java.util.Set;

import static com.vodafone.global.sdk.http.HttpCode.BAD_REQUEST_400;
import static com.vodafone.global.sdk.http.HttpCode.FORBIDDEN_403;
import static com.vodafone.global.sdk.http.HttpCode.NOT_FOUND_404;
import static com.vodafone.global.sdk.http.HttpCode.UNAUTHORIZED_401;

public class ValidatePinProcessor extends RequestProcessor {
    private String backendAppKey;
    private Optional<OAuthToken> authToken;

    public ValidatePinProcessor(Context context, Settings settings, String backendAppKey, Set<UserDetailsCallback> userDetailsCallbacks) {
        super(context, settings, userDetailsCallbacks);
        this.backendAppKey = backendAppKey;
    }

    void parseResponse(Response response) {
        int code = response.code();
        try {
            switch (code) {
                case HttpCode.OK_200:
                    notifyUserDetailUpdate(Parsers.parseUserDetails(response));
                    break;
                case BAD_REQUEST_400:
                    notifyError(new VodafoneException(VodafoneException.EXCEPTION_TYPE.REQUEST_VALIDATION_ERROR));
                    break;
                case UNAUTHORIZED_401:
                case FORBIDDEN_403:
                    notifyError(new VodafoneException(VodafoneException.EXCEPTION_TYPE.TOKEN_NOT_FOUND));
                    break;
                case NOT_FOUND_404:
                    notifyError(new VodafoneException(VodafoneException.EXCEPTION_TYPE.TOKEN_NOT_FOUND));
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

    Response queryServer(ValidatePinParameters validatePinParameters) throws IOException, JSONException {
        String androidId = Utils.getAndroidId(context);

        Uri.Builder builder = new Uri.Builder();
        Uri uri = builder.scheme(settings.apix.protocol)
                .authority(settings.apix.host)
                .path(settings.apix.path)
                .appendPath(validatePinParameters.getToken())
                .appendPath("pins")
                .appendQueryParameter("backendId", backendAppKey).build();

        ValidatePinRequestDirect request = ValidatePinRequestDirect.builder()
                .url(uri.toString())
                .accessToken(authToken.get().accessToken)
                .androidId(androidId)
                .mobileCountryCode(Utils.getMCC(context))
                .sdkId(settings.sdkId)
                .backendAppKey(backendAppKey)
                .pin(validatePinParameters.getPin())
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
            parseResponse(queryServer(validatePinParameters));
        } catch (Exception e) {
            notifyError(new VodafoneException(VodafoneException.EXCEPTION_TYPE.GENERIC_SERVER_ERROR));
        }
    }
}
