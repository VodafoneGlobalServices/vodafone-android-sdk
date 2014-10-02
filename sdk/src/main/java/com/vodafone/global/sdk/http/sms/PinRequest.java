package com.vodafone.global.sdk.http.sms;

import com.octo.android.robospice.request.okhttp.OkHttpSpiceRequest;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.vodafone.global.sdk.LogUtil;
import com.vodafone.global.sdk.RequestBuilderProvider;

import java.io.IOException;

public class PinRequest extends OkHttpSpiceRequest<Response> {
    private final String url;
    private final String accessToken;
    private RequestBuilderProvider requestBuilderProvider;

    /**
     * Provides builder for {@link PinRequest}.
     */
    public static Builder builder() {
        return new Builder();
    }

    protected PinRequest(
            String url, String accessToken, RequestBuilderProvider requestBuilderProvider
    ) {
        super(Response.class);
        this.url = url;
        this.accessToken = accessToken;
        this.requestBuilderProvider = requestBuilderProvider;
    }

    @Override
    public Response loadDataFromNetwork() throws IOException {
        Request request = requestBuilderProvider.builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("scope", "seamless_id_user_details_all") //TODO: REMOVE ONLY FOR TESTING!!!
                .addHeader("backendScopes", "seamless_id_user_details_all") //TODO: REMOVE ONLY FOR TESTING!!!
                .addHeader("x-int-opco", "DE") //TODO: REMOVE ONLY FOR TESTING!!!
                .get()
                .build();

        LogUtil.log(request);

        OkHttpClient client = getOkHttpClient();
        Response response = client.newCall(request).execute();

        LogUtil.log(response);
        return response;
    }

    /**
     * Builder for {@link PinRequest}.
     * @see PinRequest#builder()
     */
    public static class Builder {

        private String url;
        private String accessToken;
        private RequestBuilderProvider requestBuilderProvider;

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

        public PinRequest build() {
            return new PinRequest(url, accessToken, requestBuilderProvider);
        }
    }
}
