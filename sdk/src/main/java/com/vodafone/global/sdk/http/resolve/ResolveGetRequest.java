package com.vodafone.global.sdk.http.resolve;

import com.octo.android.robospice.request.okhttp.OkHttpSpiceRequest;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.vodafone.global.sdk.UserDetails;
import com.vodafone.global.sdk.http.ExpiredAccessToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.UUID;

import static com.vodafone.global.sdk.http.HttpCode.OK_200;

public class ResolveGetRequest extends OkHttpSpiceRequest<UserDetailsDTO> {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final String url;
    private final String accessToken;
    private final String androidId;
    private final String mobileCountryCode;
    private final String sdkId;
    private final String appId;
    private final UserDetailsDTO userDetailsDTO;

    /**
     * Provides builder for {@link ResolveGetRequest}.
     */
    public static Builder builder() {
        return new Builder();
    }

    protected ResolveGetRequest(
            String url, String accessToken, String androidId, String mobileCountryCode,
            String sdkId, String appId, UserDetailsDTO userDetailsDTO
    ) {
        super(UserDetailsDTO.class);
        this.url = url;
        this.accessToken = accessToken;
        this.androidId = androidId;
        this.mobileCountryCode = mobileCountryCode;
        this.sdkId = sdkId;
        this.appId = appId;
        this.userDetailsDTO = userDetailsDTO;
    }

    @Override
    public UserDetailsDTO loadDataFromNetwork() throws Exception {
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("User-Agent", sdkId)
                .addHeader("Application-ID", appId)
                .addHeader("etag", userDetailsDTO.etag)
                .addHeader("x-vf-trace-subject-id", androidId)
                .addHeader("x-vf-trace-subject-region", mobileCountryCode)
                .addHeader("x-vf-trace-source", sdkId + "" + appId)
                .addHeader("x-vf-trace-transaction-id", UUID.randomUUID().toString())
                .get()
                .build();
        OkHttpClient client = getOkHttpClient();
        Response response = client.newCall(request).execute();
        int code = response.code();
        switch (code) {
            case OK_200:
                return parseJson(response);
            case 304:
                return userDetailsDTO;
            case 403:
                JSONObject json = new JSONObject(response.body().string());
                String id = json.getString("id");
                if (id.equals("POL0002"))
                    throw new ExpiredAccessToken();
                throw new IllegalStateException(); // TODO better exception
            default:
                throw new IllegalStateException(); // TODO better exception
        }
    }

    private UserDetailsDTO parseJson(Response response) throws IOException, JSONException {
        String jsonString = response.body().string();
        UserDetails userDetails = UserDetails.fromJson(jsonString);
        String etag = response.header("etag");
        return new UserDetailsDTO(userDetails, etag);
    }

    /**
     * Builder for {@link ResolveGetRequest}.
     * @see ResolveGetRequest#builder()
     */
    public static class Builder {

        private String url;
        private String accessToken;
        private String androidId;
        private String mobileCountryCode;
        private String sdkId;
        private String appId;
        private UserDetailsDTO userDetailsDTO;

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

        public Builder userDetaildDTO(UserDetailsDTO userDetailsDTO) {
            this.userDetailsDTO = userDetailsDTO;
            return this;
        }

        public ResolveGetRequest build() {
            return new ResolveGetRequest(url, accessToken, androidId, mobileCountryCode, sdkId, appId, userDetailsDTO);
        }
    }
}
