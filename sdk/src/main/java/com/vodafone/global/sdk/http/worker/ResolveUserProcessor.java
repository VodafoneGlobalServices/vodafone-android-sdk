package com.vodafone.global.sdk.http.worker;

import android.content.Context;
import android.net.Uri;
import android.os.Message;

import com.google.common.base.Optional;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;
import com.vodafone.global.sdk.BadRequest;
import com.vodafone.global.sdk.GenericServerError;
import com.vodafone.global.sdk.IMSI;
import com.vodafone.global.sdk.MSISDN;
import com.vodafone.global.sdk.NoInternetConnection;
import com.vodafone.global.sdk.RequestBuilderProvider;
import com.vodafone.global.sdk.Settings;
import com.vodafone.global.sdk.UserDetails;
import com.vodafone.global.sdk.ResolutionCallback;
import com.vodafone.global.sdk.UserDetailsRequestParameters;
import com.vodafone.global.sdk.Utils;
import com.vodafone.global.sdk.VodafoneException;
import com.vodafone.global.sdk.VodafoneManager.MESSAGE_ID;
import com.vodafone.global.sdk.http.oauth.OAuthToken;
import com.vodafone.global.sdk.http.parser.Parsers;
import com.vodafone.global.sdk.http.resolve.ResolvePostRequestDirect;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.Manifest.permission.RECEIVE_SMS;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.vodafone.global.sdk.http.HttpCode.BAD_REQUEST_400;
import static com.vodafone.global.sdk.http.HttpCode.CREATED_201;
import static com.vodafone.global.sdk.http.HttpCode.FORBIDDEN_403;
import static com.vodafone.global.sdk.http.HttpCode.FOUND_302;
import static com.vodafone.global.sdk.http.HttpCode.NOT_FOUND_404;

public class ResolveUserProcessor extends RequestProcessor {
    public static final int RESOLUTION_COMLETED = CREATED_201;
    public static final int ONGOING_OR_VALIDATION_REQUIRED = FOUND_302;
    private String backendAppKey;
    private IMSI imsi;
    private final RequestBuilderProvider requestBuilderProvider;
    private Optional<OAuthToken> authToken;

    public ResolveUserProcessor(
            Context context,
            Worker worker,
            Settings settings,
            String backendAppKey,
            IMSI imsi,
            Set<ResolutionCallback> resolutionCallbacks,
            RequestBuilderProvider requestBuilderProvider
    ) {
        super(context, worker, settings, resolutionCallbacks);
        this.backendAppKey = backendAppKey;
        this.imsi = imsi;
        this.requestBuilderProvider = requestBuilderProvider;
    }

    @Override
    public void process(Optional<OAuthToken> authToken, Message msg) {
        this.authToken = authToken;
        if (!authToken.isPresent()) {
            requestNewToken(msg);
        } else {
            try {
              startResolve(msg);
            } catch (VodafoneException e) {
                notifyError(e);
            } catch (IOException e) {
                notifyError(new GenericServerError());
            } catch (JSONException e) {
                notifyError(new GenericServerError());
            }
        }
    }

    private void requestNewToken(Message msg) {
        worker.sendMessage(worker.createMessage(MESSAGE_ID.AUTHENTICATE.ordinal()));
        worker.sendMessage(worker.createMessage(msg));
    }

    private void startResolve(Message msg) throws IOException, JSONException {
        UserDetailsRequestParameters parameters = (UserDetailsRequestParameters) msg.obj;
        boolean smsValidation = parameters.smsValidation();
        MSISDN msisdn = parameters.getMSISDN();

        if (noInternetConnection()) {
            resolutionCantBeDoneWithoutNetworkConnection();
        } else if (connectedToInternet() && msisdn.isPresent()) {
            if (msisdn.isValid()) {
                resolveWithMsisdn(msisdn);
            } else {
                resolutionFailed();
            }
        } else if (overWifi() && imsi.isValid()) {
            resolveWithImsi(imsi, smsValidation, settings.apix);
        } else if (overMobileNetwork() && imsi.isValid()) {
            resolveWithImsi(imsi, smsValidation, settings.hap);
        } else {
            resolutionFailed();
        }
    }

    private void resolutionCantBeDoneWithoutNetworkConnection() {
        notifyError(new NoInternetConnection());
    }

    private void resolveWithMsisdn(MSISDN msisdn) throws IOException, JSONException {
        Uri uri = new Uri.Builder().scheme(settings.apix.protocol)
                .authority(settings.apix.host)
                .path(settings.apix.path)
                .appendQueryParameter("backendId", backendAppKey).build();
        String url = uri.toString();

        ResolvePostRequestDirect request = ResolvePostRequestDirect.builder()
                .url(url)
                .accessToken(authToken.get().accessToken)
                .msisdn(msisdn)
                .requestBuilderProvider(requestBuilderProvider)
                .build();
        request.setRetryPolicy(null);
        request.setOkHttpClient(new OkHttpClient());
        Response response = request.loadDataFromNetwork();
        parseResponse(worker, response);
    }

    private void resolveWithImsi(IMSI imsi, boolean smsValidation, Settings.PathSettings server) throws IOException, JSONException {
        Uri uri = new Uri.Builder().scheme(server.protocol)
                .authority(server.host)
                .path(server.path)
                .appendQueryParameter("backendId", backendAppKey).build();
        String url = uri.toString();

        ResolvePostRequestDirect request = ResolvePostRequestDirect.builder()
                .url(url)
                .accessToken(authToken.get().accessToken)
                .imsi(imsi)
                .smsValidation(smsValidation)
                .requestBuilderProvider(requestBuilderProvider)
                .build();
        request.setRetryPolicy(null);
        request.setOkHttpClient(new OkHttpClient());
        Response response = request.loadDataFromNetwork();
        parseResponse(worker, response);
    }

    private void resolutionFailed() {
        notifyUserDetailUpdate(UserDetails.RESOLUTION_FAILED);
    }

    private boolean noInternetConnection() {
        return !connectedToInternet();
    }

    private boolean connectedToInternet() {
        return overWifi() || overMobileNetwork();
    }

    private boolean overWifi() {
        return Utils.isDataOverWiFi(context);
    }

    private boolean overMobileNetwork() {
        return Utils.isDataOverMobileNetwork(context);
    }

    private void parseResponse(Worker worker, Response response) throws IOException, JSONException {
        int code = response.code();
            switch (code) {
                case RESOLUTION_COMLETED:
                    notifyUserDetailUpdate(Parsers.resolutionCompleted(response));
                    break;
                case ONGOING_OR_VALIDATION_REQUIRED:
                    String location = response.header("Location");
                    if (requiresSmsValidation(location)) {
                        if (canReadSMS()) {
                            generatePin(extractToken(location));
                        } else {
                            validationRequired(extractToken(location));
                        }
                    } else if (resolutionIsOngoing(location)) {
                        checkStatus();
                    } else {
                        notifyError(new GenericServerError());
                    }
                    break;
                case NOT_FOUND_404:
                    resolutionFailed();
                    break;
                case BAD_REQUEST_400:
                    // Application ID doesn't exist on APIX
                    // Application ID has no seamlessID scope associated
                    notifyError(new BadRequest());
                    break;
                case FORBIDDEN_403:
                    String body = response.body().string();
                    if (!body.isEmpty()) {
                        JSONObject json = new JSONObject(response.body().string());
                        String id = json.getString("id");
                        if (id.equals("POL0002")) {
                            worker.sendMessage(worker.createMessage(MESSAGE_ID.AUTHENTICATE.ordinal()));
                            worker.sendMessage(worker.createMessage(MESSAGE_ID.RETRIEVE_USER_DETAILS.ordinal()));
                        }
                    } else {
                        notifyError(new GenericServerError());
                    }
                    break;
                default:
                    notifyError(new GenericServerError());
            }
    }

    private String extractToken(String location) {
        Pattern pattern = Pattern.compile(".*/users/tokens/(.*)[/?].*");
        Matcher matcher = pattern.matcher(location);
        return matcher.group(1);
    }

    private boolean requiresSmsValidation(String location) {
        Pattern pattern = Pattern.compile(".*/users/tokens/[^/]*/pins\\?backendId=.*");
        Matcher matcher = pattern.matcher(location);
        return matcher.matches();
    }

    private boolean resolutionIsOngoing(String location) {
        Pattern pattern = Pattern.compile(".*/users/tokens/[^/]*\\?backendId=.*");
        Matcher matcher = pattern.matcher(location);
        return matcher.matches();
    }

    private void validationRequired(String token) {
        notifyUserDetailUpdate(UserDetails.validationRequired(token));
    }

    private void checkStatus() {
        worker.sendMessage(worker.createMessage(MESSAGE_ID.CHECK_STATUS.ordinal()));
    }

    private void generatePin(String token) {
        worker.sendMessage(worker.createMessage(MESSAGE_ID.GENERATE_PIN.ordinal(), token));
    }

    private boolean canReadSMS() {
        return context.checkCallingOrSelfPermission(RECEIVE_SMS) == PERMISSION_GRANTED;
    }
}
