package com.vodafone.global.sdk.http.resolve;

import com.squareup.okhttp.*;
import com.vodafone.global.sdk.IMSI;
import com.vodafone.global.sdk.MSISDN;
import com.vodafone.global.sdk.RequestBuilderProvider;
import com.vodafone.global.sdk.http.ResponseHolder;
import com.vodafone.global.sdk.logging.LogUtil;
import com.vodafone.global.sdk.logging.Logger;
import org.json.JSONException;

import java.io.IOException;

public abstract class ResolvePostRequest extends com.vodafone.global.sdk.http.Request {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final String url;
    private final String accessToken;
    protected final boolean smsValidation;
    private RequestBuilderProvider requestBuilderProvider;
    private final Logger logger;

    /**
     * Provides builder for {@link ResolvePostRequest}.
     */
    public static Builder builder() {
        return new Builder();
    }

    protected ResolvePostRequest(
            String url, String accessToken,
            boolean smsValidation,
            RequestBuilderProvider requestBuilderProvider,
            Logger logger
    ) {
        this.url = url;
        this.accessToken = accessToken;
        this.smsValidation = smsValidation;
        this.requestBuilderProvider = requestBuilderProvider;
        this.logger = logger;
    }

    @Override
    public ResponseHolder loadDataFromNetwork() throws IOException, JSONException {
        String content = prepareBody();
        RequestBody body = RequestBody.create(JSON, content);
        Request request = requestBuilderProvider.builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .post(body)
                .build();

        logger.d(LogUtil.prepareRequestLogMsg(request));

        OkHttpClient client = getOkHttpClient();
        client.setFollowRedirects(false);
        Response response = client.newCall(request).execute();

        ResponseHolder responseHolder = new ResponseHolder(response);
        logger.d(LogUtil.prepareResponseLogMsg(responseHolder));
        return responseHolder;
    }

    protected abstract String prepareBody() throws JSONException;

    /**
     * Builder for {@link ResolvePostRequest}.
     * @see ResolvePostRequest#builder()
     */
    public static class Builder {

        private String url;
        private String accessToken;
        private MSISDN msisdn;
        private IMSI imsi;
        private boolean smsValidation;
        private RequestBuilderProvider requestBuilderProvider;
        private Logger logger;

        private Builder() {
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public Builder imsi(IMSI imsi) {
            this.imsi = imsi;
            return this;
        }

        public Builder smsValidation(boolean smsValidation) {
            this.smsValidation = smsValidation;
            return this;
        }

        public Builder msisdn(MSISDN msisdn) {
            this.msisdn = msisdn;
            return this;
        }

        public Builder requestBuilderProvider(RequestBuilderProvider requestBuilderProvider) {
            this.requestBuilderProvider = requestBuilderProvider;
            return this;
        }

        public Builder logger(Logger logger) {
            this.logger = logger;
            return this;
        }

        public ResolvePostRequest build() {
            if (msisdn != null && imsi == null)
                return new ResolvePostMsisdnRequest(url, accessToken, msisdn, smsValidation, requestBuilderProvider, logger);
            else if (imsi != null && msisdn == null)
                return new ResolvePostImsiRequest(url, accessToken, imsi, smsValidation, requestBuilderProvider, logger);
            else
                throw new IllegalStateException("bad usage of request builder");
        }
    }
}
