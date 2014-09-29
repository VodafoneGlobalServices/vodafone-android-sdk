package com.vodafone.global.sdk.http.worker;

import android.content.Context;
import android.net.Uri;
import android.os.Message;

import com.google.common.base.Optional;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;
import com.vodafone.global.sdk.GenericServerError;
import com.vodafone.global.sdk.RequestBuilderProvider;
import com.vodafone.global.sdk.RequestValidationError;
import com.vodafone.global.sdk.Settings;
import com.vodafone.global.sdk.TokenNotFound;
import com.vodafone.global.sdk.ResolutionCallback;
import com.vodafone.global.sdk.Utils;
import com.vodafone.global.sdk.ValidatePinParameters;
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
    private final RequestBuilderProvider requestBuilderProvider;
    private Optional<OAuthToken> authToken;

    public ValidatePinProcessor(
            Context context,
            Worker worker,
            Settings settings,
            String backendAppKey,
            Set<ResolutionCallback> resolutionCallbacks,
            RequestBuilderProvider requestBuilderProvider
    ) {
        super(context, worker, settings, resolutionCallbacks);
        this.backendAppKey = backendAppKey;
        this.requestBuilderProvider = requestBuilderProvider;
    }

    void parseResponse(Response response) {
        int code = response.code();
        try {
            switch (code) {
                case HttpCode.OK_200:
                    notifyUserDetailUpdate(Parsers.parseUserDetails(response));
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
        } catch (JSONException e) {
            notifyError(new GenericServerError());
        } catch (IOException e) {
            notifyError(new GenericServerError());
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
                .pin(validatePinParameters.getPin())
                .requestBuilderProvider(requestBuilderProvider)
                .build();

        request.setRetryPolicy(null);
        request.setOkHttpClient(new OkHttpClient());
        return request.loadDataFromNetwork();
    }

    @Override
    public void process(Optional<OAuthToken> authToken, Message msg) {
        ValidatePinParameters validatePinParameters = (ValidatePinParameters) msg.obj;

        try {
            this.authToken = authToken;
            parseResponse(queryServer(validatePinParameters));
        } catch (Exception e) {
            notifyError(new GenericServerError());
        }
    }
}
