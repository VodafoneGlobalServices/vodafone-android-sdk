package com.vodafone.global.sdk.http.resolve;

import com.octo.android.robospice.request.okhttp.OkHttpSpiceRequest;
import com.squareup.okhttp.*;
import com.vodafone.global.sdk.IMSI;
import com.vodafone.global.sdk.LogUtil;
import com.vodafone.global.sdk.MSISDN;
import com.vodafone.global.sdk.RequestBuilderProvider;
import org.json.JSONException;

import java.io.IOException;

public abstract class ResolvePostRequest extends OkHttpSpiceRequest<Response> {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final String url;
    private final String accessToken;
    protected final boolean smsValidation;
    private RequestBuilderProvider requestBuilderProvider;

    /**
     * Provides builder for {@link ResolvePostRequest}.
     */
    public static Builder builder() {
        return new Builder();
    }

    protected ResolvePostRequest(
            String url, String accessToken,
            boolean smsValidation,
            RequestBuilderProvider requestBuilderProvider
    ) {
        super(Response.class);
        this.url = url;
        this.accessToken = accessToken;
        this.smsValidation = smsValidation;
        this.requestBuilderProvider = requestBuilderProvider;
    }

    @Override
    public Response loadDataFromNetwork() throws IOException, JSONException {
        String content = prepareBody();
        RequestBody body = RequestBody.create(JSON, content);
        Request request = requestBuilderProvider.builder()
                .url(url)
                .addHeader("scope", "seamless_id_user_details_all") //TODO: REMOVE ONLY FOR TESTING!!!
                .addHeader("backendScopes", "seamless_id_user_details_all") //TODO: REMOVE ONLY FOR TESTING!!!
                .addHeader("x-int-opco", "DE") //TODO: REMOVE ONLY FOR TESTING!!!
                .addHeader("Authorization", "Bearer " + accessToken)
                .post(body)
                .build();

        LogUtil.log(request);

        OkHttpClient client = getOkHttpClient();
        client.setFollowRedirects(false);
        Response response = client.newCall(request).execute();

        LogUtil.log(response);
        return response;
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

        public ResolvePostRequest build() {
            if (msisdn != null && imsi == null)
                return new ResolvePostMsisdnRequest(url, accessToken, msisdn, smsValidation, requestBuilderProvider);
            else if (imsi != null && msisdn == null)
                return new ResolvePostImsiRequest(url, accessToken, imsi, smsValidation, requestBuilderProvider);
            else
                throw new IllegalStateException("bad usage of request builder");
        }
    }
}
