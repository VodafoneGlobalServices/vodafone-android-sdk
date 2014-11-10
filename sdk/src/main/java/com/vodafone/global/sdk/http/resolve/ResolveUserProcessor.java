package com.vodafone.global.sdk.http.resolve;

import android.content.Context;
import android.net.Uri;
import android.os.Message;
import com.google.common.base.Optional;
import com.squareup.okhttp.OkHttpClient;
import com.vodafone.global.sdk.*;
import com.vodafone.global.sdk.http.GenericServerError;
import com.vodafone.global.sdk.http.NoInternetConnection;
import com.vodafone.global.sdk.http.ResponseHolder;
import com.vodafone.global.sdk.http.oauth.OAuthToken;
import com.vodafone.global.sdk.http.sms.InvalidInput;
import com.vodafone.global.sdk.logging.Logger;
import org.json.JSONException;

import java.io.IOException;

import static com.vodafone.global.sdk.MessageType.AUTHENTICATE;

public class ResolveUserProcessor {
    protected final Worker worker;
    protected final Settings settings;
    protected final Context context;
    protected final ResolveCallbacks resolveCallbacks;
    private String backendAppKey;
    private IMSI imsi;
    private final RequestBuilderProvider requestBuilderProvider;
    private final Logger logger;
    private Optional<OAuthToken> authToken;
    private final ResolveUserParser parser;

    public ResolveUserProcessor(
            Context context,
            Worker worker,
            Settings settings,
            String backendAppKey,
            IMSI imsi,
            ResolveCallbacks resolveCallbacks,
            RequestBuilderProvider requestBuilderProvider,
            Logger logger
    ) {
        this.context = context;
        this.worker = worker;
        this.settings = settings;
        this.resolveCallbacks = resolveCallbacks;
        this.backendAppKey = backendAppKey;
        this.imsi = imsi;
        this.requestBuilderProvider = requestBuilderProvider;
        this.logger = logger;
        parser = new ResolveUserParser(worker, context, resolveCallbacks);
    }

    public void process(Optional<OAuthToken> authToken, Message msg) {
        this.authToken = authToken;
        if (!authToken.isPresent()) {
            logger.w("OAuth token needs to be refreshed");
            requestNewToken(msg);
        } else {
            try {
              startResolve(msg);
            } catch (VodafoneException e) {
                resolveCallbacks.notifyError(e);
            } catch (IOException e) {
                resolveCallbacks.notifyError(new GenericServerError(e));
            } catch (JSONException e) {
                resolveCallbacks.notifyError(new GenericServerError(e));
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
        MSISDN msisdn = new MSISDN(parameters.getMSISDN(), settings.availableMarkets());

        if (noInternetConnection()) {
            resolutionCantBeDoneWithoutNetworkConnection();
        } else if (connectedToInternet() && msisdn.isPresent()) {
            logger.d("MSISDN is present");
            if (msisdn.isValid()) {
                logger.d("MSISDN is valid");
                resolveWithMsisdn(msisdn);
            } else {
                logger.d("MSISDN is invalid");
                resolveCallbacks.notifyError(new InvalidInput());
            }
        } else if (overWifi() && imsi.isValid()) {
            resolveThroughApix(smsValidation);
        } else if (overMobileNetwork() && imsi.mccAndMncOfSimCardBelongToVodafone()) {
            logger.d("over mobile network, MCC and MNC belongs to Vodafone");
            resolveThroughHap(smsValidation);
        } else {
            logger.w("no IMSI and no MSISDN, MCC and MNC don't belong to Vodafone");
            resolveCallbacks.unableToResolve();
        }
    }

    private void resolveThroughApix(boolean smsValidation) throws IOException, JSONException {
        logger.i("over wifi, resolving through APIX");
        resolveWithImsi(imsi, smsValidation, settings.apix());
    }

    private void resolveThroughHap(boolean smsValidation) throws IOException, JSONException {
        logger.i("over mobile, resolving through HAP");
        resolveWithImsi(imsi, smsValidation, settings.hap());
    }

    private void resolutionCantBeDoneWithoutNetworkConnection() {
        resolveCallbacks.notifyError(new NoInternetConnection());
    }

    private void resolveWithMsisdn(MSISDN msisdn) throws IOException, JSONException {
        logger.i("MSISDN is valid, resolving with MSISDN");

        String url = new Uri.Builder()
                .scheme(settings.apix().protocol())
                .authority(settings.apix().host())
                .path(settings.apix().path())
                .appendQueryParameter("backendId", backendAppKey)
                .build()
                .toString();

        ResolvePostRequest request = ResolvePostRequest.builder()
                .url(url)
                .accessToken(authToken.get().accessToken)
                .msisdn(msisdn)
                .requestBuilderProvider(requestBuilderProvider)
                .logger(logger)
                .build();
        request.setOkHttpClient(new OkHttpClient());
        ResponseHolder response = request.loadDataFromNetwork();
        parser.parseResponse(response);
    }

    private void resolveWithImsi(IMSI imsi, boolean smsValidation, Settings.PathSettings server) throws IOException, JSONException {
        String url = new Uri.Builder()
                .scheme(server.protocol())
                .authority(server.host())
                .path(server.path())
                .appendQueryParameter("backendId", backendAppKey)
                .build()
                .toString();

        ResolvePostRequest request = ResolvePostRequest.builder()
                .url(url)
                .accessToken(authToken.get().accessToken)
                .imsi(imsi)
                .smsValidation(smsValidation)
                .requestBuilderProvider(requestBuilderProvider)
                .logger(logger)
                .build();
        request.setOkHttpClient(new OkHttpClient());
        ResponseHolder responseHolder = request.loadDataFromNetwork();
        parser.parseResponse(responseHolder);
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
