package com.vodafone.global.sdk.http.resolve;

import com.octo.android.robospice.request.okhttp.OkHttpSpiceRequest;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.vodafone.global.sdk.SimSerialNumber;
import com.vodafone.global.sdk.UserDetails;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.UUID;

public class ResolvePostRequest extends OkHttpSpiceRequest<UserDetailsDTO> {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final String url;
    private final String accessToken;
    private final String androidId;
    private final String mobileCountryCode;
    private final String sdkId;
    private final String appId;
    private final SimSerialNumber imsi;
    private final boolean smsValidation;

    /**
     * Provides builder for {@link ResolvePostRequest}.
     */
    public static Builder builder() {
        return new Builder();
    }

    protected ResolvePostRequest(
            String url, String accessToken, String androidId, String mobileCountryCode,
            String sdkId, String appId, SimSerialNumber imsi, boolean smsValidation
    ) {
        super(UserDetailsDTO.class);
        this.url = url;
        this.accessToken = accessToken;
        this.androidId = androidId;
        this.mobileCountryCode = mobileCountryCode;
        this.sdkId = sdkId;
        this.appId = appId;
        this.imsi = imsi;
        this.smsValidation = smsValidation;
    }

    @Override
    public UserDetailsDTO loadDataFromNetwork() throws Exception {
        RequestBody body = RequestBody.create(JSON, prepareBody(imsi, smsValidation));
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("User-Agent", sdkId)
                .addHeader("Application-ID", appId)
                .addHeader("x-vf-trace-subject-id", androidId)
                .addHeader("x-vf-trace-subject-region", mobileCountryCode)
                .addHeader("x-vf-trace-source", sdkId + "" + appId)
                .addHeader("x-vf-trace-transaction-id", UUID.randomUUID().toString())
                .post(body)
                .build();
        OkHttpClient client = getOkHttpClient();
        Response response = client.newCall(request).execute();
        int code = response.code();
        switch (code) {
            case 200:
                return parseJson(response);
            default:
                throw new IllegalStateException(); // TODO better exception
        }
    }

    protected String prepareBody(SimSerialNumber imsi, boolean smsValidation) throws JSONException {
        JSONObject json = new JSONObject();
        if (imsi.isPresent()) {
            json.put("IMSI", imsi.get());
        }
        json.put("SMSValidation", smsValidation);
        return json.toString();
    }

    private UserDetailsDTO parseJson(Response response) throws IOException, JSONException {
        String jsonString = response.body().string();
        UserDetails userDetails = UserDetails.fromJson(jsonString);
        String etag = response.header("etag");
        return new UserDetailsDTO(userDetails, etag);
    }

    /**
     * Builder for {@link ResolvePostRequest}.
     * @see ResolvePostRequest#builder()
     */
    public static class Builder {

        private String url;
        private String accessToken;
        private String androidId;
        private String mobileCountryCode;
        private String sdkId;
        private String appId;
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

        public ResolvePostRequest build() {
            return new ResolvePostRequest(url, accessToken, androidId, mobileCountryCode, sdkId, appId, imsi, smsValidation);
        }
    }
}
