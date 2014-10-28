package com.vodafone.global.sdk.http.resolve;

import com.octo.android.robospice.request.okhttp.OkHttpSpiceRequest;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.vodafone.global.sdk.logging.LogUtil;
import com.vodafone.global.sdk.RequestBuilderProvider;
import com.vodafone.global.sdk.logging.Logger;

import java.io.IOException;

public class ResolveGetRequest extends OkHttpSpiceRequest<Response> {
    private final String url;
    private final String accessToken;
    private final String etag;
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
            String etag,
            RequestBuilderProvider requestBuilderProvider,
            Logger logger
    ) {
        super(Response.class);
        this.url = url;
        this.accessToken = accessToken;
        this.etag = etag;
        this.requestBuilderProvider = requestBuilderProvider;
        this.logger = logger;
    }

    @Override
    public Response loadDataFromNetwork() throws IOException {
        Request request = requestBuilderProvider.builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("etag", etag)
                .get()
                .build();

        logger.d(LogUtil.prepareRequestLogMsg(request));

        OkHttpClient client = getOkHttpClient();
        Response response = client.newCall(request).execute();

        logger.d(LogUtil.prepareResponseLogMsg(response));
        return response;
    }

    /**
     * Builder for {@link ResolveGetRequest}.
     * @see ResolveGetRequest#builder()
     */
    public static class Builder {

        private String url;
        private String accessToken;
        private String etag;
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

        public Builder etag(String etag) {
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
