package com.vodafone.global.sdk.http.sms;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.vodafone.global.sdk.RequestBuilderProvider;
import com.vodafone.global.sdk.http.ResponseHolder;
import com.vodafone.global.sdk.logging.LogUtil;
import com.vodafone.global.sdk.logging.Logger;

import java.io.IOException;

public class PinRequest extends com.vodafone.global.sdk.http.Request {
    private final String url;
    private final String accessToken;
    private RequestBuilderProvider requestBuilderProvider;
    private final Logger logger;

    /**
     * Provides builder for {@link PinRequest}.
     */
    public static Builder builder() {
        return new Builder();
    }

    protected PinRequest(
            String url,
            String accessToken,
            RequestBuilderProvider requestBuilderProvider,
            Logger logger
    ) {
        this.url = url;
        this.accessToken = accessToken;
        this.requestBuilderProvider = requestBuilderProvider;
        this.logger = logger;
    }

    @Override
    public ResponseHolder loadDataFromNetwork() throws IOException {
        Request request = requestBuilderProvider.builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .get()
                .build();

        logger.d(LogUtil.prepareRequestLogMsg(request));

        OkHttpClient client = getOkHttpClient();
        Response response = client.newCall(request).execute();

        ResponseHolder responseHolder = new ResponseHolder(response);
        logger.d(LogUtil.prepareResponseLogMsg(responseHolder));
        return responseHolder;
    }

    /**
     * Builder for {@link PinRequest}.
     * @see PinRequest#builder()
     */
    public static class Builder {

        private String url;
        private String accessToken;
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

        public Builder requestBuilderProvider(RequestBuilderProvider requestBuilderProvider) {
            this.requestBuilderProvider = requestBuilderProvider;
            return this;
        }

        public Builder logger(Logger logger) {
            this.logger = logger;
            return this;
        }

        public PinRequest build() {
            return new PinRequest(url, accessToken, requestBuilderProvider, logger);
        }
    }
}
