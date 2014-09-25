package com.vodafone.global.sdk.http.resolve;

import com.octo.android.robospice.request.okhttp.OkHttpSpiceRequest;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.vodafone.global.sdk.IMSI;
import com.vodafone.global.sdk.LogUtil;
import com.vodafone.global.sdk.RequestBuilderProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ResolvePostRequestDirect extends OkHttpSpiceRequest<Response> {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final String url;
    private final String accessToken;
    private final IMSI imsi;
    private final String msisdn;
    private final String market;
    private final boolean smsValidation;
    private RequestBuilderProvider requestBuilderProvider;

    /**
     * Provides builder for {@link com.vodafone.global.sdk.http.resolve.ResolvePostRequestDirect}.
     */
    public static Builder builder() {
        return new Builder();
    }

    protected ResolvePostRequestDirect(
            String url, String accessToken,
            String msisdn, String market, IMSI imsi, boolean smsValidation,
            RequestBuilderProvider requestBuilderProvider
    ) {
        super(Response.class);
        this.url = url;
        this.accessToken = accessToken;
        this.imsi = imsi;
        this.smsValidation = smsValidation;
        this.msisdn = msisdn;
        this.market = market;
        this.requestBuilderProvider = requestBuilderProvider;
    }

    @Override
    public Response loadDataFromNetwork() throws IOException, JSONException {
        String content = prepareBody(msisdn, market, imsi, smsValidation);
        RequestBody body = RequestBody.create(JSON, content);
        Request request = requestBuilderProvider.builder()
                .url(url)
                .addHeader("scope", "seamless_id_user_details_all") //TODO: REMOVE ONLY FOR TESTING!!!
                .addHeader("backendScopes", "seamless_id_user_details_all") //TODO: REMOVE ONLY FOR TESTING!!!
                .addHeader("x-int-opco", market) //TODO: REMOVE ONLY FOR TESTING!!!
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

    protected String prepareBody(String msisdn, String market, IMSI imsi, boolean smsValidation) throws JSONException {
        JSONObject json = new JSONObject();
        if (imsi.isPresent()) {
            //json.put("imsi", imsi.get());
            json.put("imsi", "204049810027400"); //TODO: REMOVE ONLY FOR TESTING!!!
        }
        if (!msisdn.isEmpty() && !market.isEmpty()) {
            json.put("msisdn", msisdn);
            json.put("market", market);
        }
        json.put("smsValidation", smsValidation);
        return json.toString();
    }

    /**
     * Builder for {@link com.vodafone.global.sdk.http.resolve.ResolvePostRequestDirect}.
     * @see com.vodafone.global.sdk.http.resolve.ResolvePostRequestDirect#builder()
     */
    public static class Builder {

        private String url;
        private String accessToken;
        private String msisdn;
        private String market;
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

        public Builder msisdn(String  msisdn) {
            this.msisdn = msisdn;
            return this;
        }

        public Builder market(String market) {
            this.market = market;
            return this;
        }

        public Builder requestBuilderProvider(RequestBuilderProvider requestBuilderProvider) {
            this.requestBuilderProvider = requestBuilderProvider;
            return this;
        }

        public ResolvePostRequestDirect build() {
            return new ResolvePostRequestDirect(url, accessToken, msisdn, market, imsi, smsValidation, requestBuilderProvider);
        }
    }
}
