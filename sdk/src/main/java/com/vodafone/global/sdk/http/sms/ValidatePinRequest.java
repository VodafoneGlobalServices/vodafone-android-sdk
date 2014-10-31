package com.vodafone.global.sdk.http.sms;


import com.octo.android.robospice.request.okhttp.OkHttpSpiceRequest;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.vodafone.global.sdk.http.ResponseHolder;
import com.vodafone.global.sdk.logging.LogUtil;
import com.vodafone.global.sdk.RequestBuilderProvider;

import com.vodafone.global.sdk.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ValidatePinRequest extends OkHttpSpiceRequest<ResponseHolder> {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final String url;
    private final String accessToken;
    private final String pin;
    private final RequestBuilderProvider requestBuilderProvider;
    private final Logger logger;

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
            RequestBuilderProvider requestBuilderProvider,
            Logger logger
    ) {
        super(ResponseHolder.class);
        this.url = url;
        this.accessToken = accessToken;
        this.pin = pin;
        this.requestBuilderProvider = requestBuilderProvider;
        this.logger = logger;
    }

    @Override
    public ResponseHolder loadDataFromNetwork() throws IOException, JSONException {
        RequestBody body = RequestBody.create(JSON, prepareBody(pin));
        Request request = requestBuilderProvider.builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .post(body)
                .build();

        logger.d(LogUtil.prepareRequestLogMsg(request));

        OkHttpClient client = getOkHttpClient();
        Response response = client.newCall(request).execute();

        ResponseHolder responseHolder = new ResponseHolder(response);
        logger.d(LogUtil.prepareResponseLogMsg(responseHolder));
        return responseHolder;
    }

    protected String prepareBody(String pin) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("code", pin);
        return json.toString();
    }

    public static class Builder {

        private String url;
        private String accessToken;
        private String pin;
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

        public Builder pin(String pin) {
            this.pin = pin;
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

        public ValidatePinRequest build() {
            return new ValidatePinRequest(url, accessToken, pin, requestBuilderProvider, logger);
        }
    }
}
