package com.vodafone.global.sdk.http.resolve;

import com.octo.android.robospice.request.okhttp.OkHttpSpiceRequest;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.vodafone.global.sdk.RequestBuilderProvider;
import com.vodafone.global.sdk.ResolutionStatus;
import com.vodafone.global.sdk.SimSerialNumber;
import com.vodafone.global.sdk.UserDetails;
import com.vodafone.global.sdk.http.ExpiredAccessToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import static com.vodafone.global.sdk.http.HttpCode.FORBIDDEN_403;
import static com.vodafone.global.sdk.http.HttpCode.OK_200;

public class ResolvePostRequest extends OkHttpSpiceRequest<UserDetailsDTO> {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final String url;
    private final String accessToken;
    private final SimSerialNumber imsi;
    private final boolean smsValidation;
    private RequestBuilderProvider requestBuilderProvider;

    /**
     * Provides builder for {@link ResolvePostRequest}.
     */
    public static Builder builder() {
        return new Builder();
    }

    protected ResolvePostRequest(
            String url, String accessToken,
            SimSerialNumber imsi,
            boolean smsValidation,
            RequestBuilderProvider requestBuilderProvider
    ) {
        super(UserDetailsDTO.class);
        this.url = url;
        this.accessToken = accessToken;
        this.imsi = imsi;
        this.smsValidation = smsValidation;
        this.requestBuilderProvider = requestBuilderProvider;
    }

    @Override
    public UserDetailsDTO loadDataFromNetwork() throws Exception {
        RequestBody body = RequestBody.create(JSON, prepareBody(imsi, smsValidation));
        Request request = requestBuilderProvider.builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .post(body)
                .build();
        OkHttpClient client = getOkHttpClient();
        Response response = client.newCall(request).execute();
        int code = response.code();
        switch (code) {
            case OK_200:
                return parseJson(response);
            case FORBIDDEN_403:
                JSONObject json = new JSONObject(response.body().string());
                String id = json.getString("id");
                if (id.equals("POL0002"))
                    throw new ExpiredAccessToken();
                throw new IllegalStateException(); // TODO better exception
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
        UserDetails userDetails = UserDetails.fromJson(jsonString, ResolutionStatus.FIXME);
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
        private SimSerialNumber imsi;
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

        public Builder imsi(SimSerialNumber imsi) {
            this.imsi = imsi;
            return this;
        }

        public Builder smsValidation(boolean smsValidation) {
            this.smsValidation = smsValidation;
            return this;
        }

        public Builder requestBuilderProvider(RequestBuilderProvider requestBuilderProvider) {
            this.requestBuilderProvider = requestBuilderProvider;
            return this;
        }

        public ResolvePostRequest build() {
            return new ResolvePostRequest(url, accessToken, imsi, smsValidation, requestBuilderProvider);
        }
    }
}
