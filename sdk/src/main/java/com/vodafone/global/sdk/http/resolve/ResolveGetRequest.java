package com.vodafone.global.sdk.http.resolve;

import com.google.common.base.Optional;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.vodafone.global.sdk.RequestBuilderProvider;
import com.vodafone.global.sdk.http.ResponseHolder;
import com.vodafone.global.sdk.logging.LogUtil;
import com.vodafone.global.sdk.logging.Logger;

import java.io.IOException;

public class ResolveGetRequest extends com.vodafone.global.sdk.http.Request {
    private final String url;
    private final String accessToken;
    private final Optional<String> etag;
    private final RequestBuilderProvider requestBuilderProvider;
    private final Logger logger;

    /**
     * Provides builder for {@link ResolveGetRequest}.
     */
    public static Builder builder() {
        return new Builder();
    }

    protected ResolveGetRequest(
            String url,
            String accessToken,
            Optional<String> etag,
            RequestBuilderProvider requestBuilderProvider,
            Logger logger
    ) {
        this.url = url;
        this.accessToken = accessToken;
        this.etag = etag;
        this.requestBuilderProvider = requestBuilderProvider;
        this.logger = logger;
    }

    @Override
    public ResponseHolder loadDataFromNetwork() throws IOException {
        Request.Builder builder = requestBuilderProvider.builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken);
        if (etag.isPresent()) {
            builder.addHeader("If-None-Match", etag.get());
        }
        Request request = builder.get().build();

        logger.d(LogUtil.prepareRequestLogMsg(request));

        OkHttpClient client = getOkHttpClient();
        Response response = client.newCall(request).execute();

        ResponseHolder responseHolder = new ResponseHolder(response);
        logger.d(LogUtil.prepareResponseLogMsg(responseHolder));
        return responseHolder;
    }

    /**
     * Builder for {@link ResolveGetRequest}.
     * @see ResolveGetRequest#builder()
     */
    public static class Builder {

        private String url;
        private String accessToken;
        private Optional<String> etag;
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

        public Builder etag(Optional<String> etag) {
            this.etag = etag;
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

        public ResolveGetRequest build() {
            return new ResolveGetRequest(url, accessToken, etag, requestBuilderProvider, logger);
        }
    }
}
