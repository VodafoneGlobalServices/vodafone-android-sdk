package com.vodafone.global.sdk.http.sms;


import com.octo.android.robospice.request.okhttp.OkHttpSpiceRequest;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.vodafone.global.sdk.RequestBuilderProvider;
import com.vodafone.global.sdk.http.ExpiredAccessToken;

import org.json.JSONException;
import org.json.JSONObject;

import static com.vodafone.global.sdk.http.HttpCode.FORBIDDEN_403;
import static com.vodafone.global.sdk.http.HttpCode.OK_200;

public class ValidatePinRequest extends OkHttpSpiceRequest<Void> {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final String url;
    private final String accessToken;
    private final String pin;
    private RequestBuilderProvider requestBuilderProvider;

    /**
     * Provides builder for {@link ValidatePinRequest}.
     */
    public static Builder builder() {
        return new Builder();
    }

    protected ValidatePinRequest(
            String url,
            String accessToken,
            String pin,
            RequestBuilderProvider requestBuilderProvider
    ) {
        super(Void.class);
        this.url = url;
        this.accessToken = accessToken;
        this.pin = pin;
        this.requestBuilderProvider = requestBuilderProvider;
    }

    @Override
    public Void loadDataFromNetwork() throws Exception {
        // e.g. POST https://APIX/he/users/tokens/validate/{token}
        RequestBody emptyBody = RequestBody.create(JSON, prepareBody(pin));
        Request request = requestBuilderProvider.builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .post(emptyBody)
                .build();
        OkHttpClient client = getOkHttpClient();
        Response response = client.newCall(request).execute();
        int code = response.code();
        switch (code) {
            case OK_200:
                // we are only interested in HTTP code, body is empty anyway so we return null
                return null;
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

    private String prepareBody(String pin) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("pin", pin);
        return json.toString();
    }

    public static class Builder {

        private String url;
        private String accessToken;
        private String pin;
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

        public ValidatePinRequest build() {
            return new ValidatePinRequest(url, accessToken, pin, requestBuilderProvider);
        }
    }
}
