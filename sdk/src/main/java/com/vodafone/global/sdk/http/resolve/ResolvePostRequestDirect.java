package com.vodafone.global.sdk.http.resolve;

import com.octo.android.robospice.request.okhttp.OkHttpSpiceRequest;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.vodafone.global.sdk.LogUtil;
import com.vodafone.global.sdk.SimSerialNumber;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.UUID;

public class ResolvePostRequestDirect extends OkHttpSpiceRequest<Response> {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final String url;
    private final String accessToken;
    private final String androidId;
    private final String mobileCountryCode;
    private final String sdkId;
    private final String appId;
    private final SimSerialNumber imsi;
    private final String msisdn;
    private final String market;
    private final boolean smsValidation;

    /**
     * Provides builder for {@link com.vodafone.global.sdk.http.resolve.ResolvePostRequestDirect}.
     */
    public static Builder builder() {
        return new Builder();
    }

    protected ResolvePostRequestDirect(
            String url, String accessToken, String androidId, String mobileCountryCode,
            String sdkId, String appId, String msisdn, String market, SimSerialNumber imsi, boolean smsValidation
    ) {
        super(Response.class);
        this.url = url;
        this.accessToken = accessToken;
        this.androidId = androidId;
        this.mobileCountryCode = mobileCountryCode;
        this.sdkId = sdkId;
        this.appId = appId;
        this.imsi = imsi;
        this.smsValidation = smsValidation;
        this.msisdn = msisdn;
        this.market = market;
    }

    @Override
    public Response loadDataFromNetwork() throws IOException, JSONException {
        String content = prepareBody(msisdn, market, imsi, smsValidation);
        RequestBody body = RequestBody.create(JSON, content);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("User-Agent", sdkId)
                .addHeader("scope", "seamless_id_user_details_all") //TODO: REMOVE ONLY FOR TESTING!!!
                .addHeader("backendScopes", "seamless_id_user_details_all") //TODO: REMOVE ONLY FOR TESTING!!!
                .addHeader("x-int-opco", market) //TODO: REMOVE ONLY FOR TESTING!!!
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("x-vf-trace-subject-id", androidId)
                .addHeader("x-vf-trace-subject-region", mobileCountryCode)
                .addHeader("x-vf-trace-source", sdkId + "-" + appId)
                .addHeader("x-vf-trace-transaction-id", UUID.randomUUID().toString())
                .post(body)
                .build();

        LogUtil.log(request);

        OkHttpClient client = getOkHttpClient();
        client.setFollowRedirects(false);
        return client.newCall(request).execute();
    }

    protected String prepareBody(String msisdn, String market, SimSerialNumber imsi, boolean smsValidation) throws JSONException {
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
        private String androidId;
        private String mobileCountryCode;
        private String sdkId;
        private String appId;
        private String msisdn;
        private String market;
        private SimSerialNumber imsi;
        private boolean smsValidation;

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

        public Builder androidId(String androidId) {
            this.androidId = androidId;
            return this;
        }

        public Builder mobileCountryCode(String mobileCountryCode) {
            this.mobileCountryCode = mobileCountryCode;
            return this;
        }

        public Builder sdkId(String sdkId) {
            this.sdkId = sdkId;
            return this;
        }

        public Builder appId(String appId) {
            this.appId = appId;
            return this;
        }

        public Builder imsi(SimSerialNumber imsi) {
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

        public ResolvePostRequestDirect build() {
            return new ResolvePostRequestDirect(url, accessToken, androidId, mobileCountryCode, sdkId, appId, msisdn, market, imsi, smsValidation);
        }
    }
}
