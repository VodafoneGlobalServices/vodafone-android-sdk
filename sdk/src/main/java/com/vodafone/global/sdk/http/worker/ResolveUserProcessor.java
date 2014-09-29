package com.vodafone.global.sdk.http.worker;

import android.content.Context;
import android.net.Uri;
import android.os.Message;
import com.google.common.base.Optional;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;
import com.vodafone.global.sdk.*;
import com.vodafone.global.sdk.http.oauth.OAuthToken;
import com.vodafone.global.sdk.http.resolve.ResolvePostRequestDirect;
import org.json.JSONException;

import java.io.IOException;
import java.util.Set;

import static com.vodafone.global.sdk.MessageType.AUTHENTICATE;

public class ResolveUserProcessor extends RequestProcessor {
    private String backendAppKey;
    private IMSI imsi;
    private final RequestBuilderProvider requestBuilderProvider;
    private Optional<OAuthToken> authToken;
    private final ResolveUserParser parser;

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
        parser = new ResolveUserParser(worker, context, resolutionCallbacks);
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
        worker.sendMessage(worker.createMessage(AUTHENTICATE));
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
        parser.parseResponse(response);
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
        parser.parseResponse(response);
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
}
