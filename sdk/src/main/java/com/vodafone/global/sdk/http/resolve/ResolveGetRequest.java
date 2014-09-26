package com.vodafone.global.sdk.http.resolve;

import com.octo.android.robospice.request.okhttp.OkHttpSpiceRequest;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.vodafone.global.sdk.RequestBuilderProvider;
import com.vodafone.global.sdk.ResolutionStatus;
import com.vodafone.global.sdk.UserDetails;
import com.vodafone.global.sdk.http.ExpiredAccessToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import static com.vodafone.global.sdk.http.HttpCode.FORBIDDEN_403;
import static com.vodafone.global.sdk.http.HttpCode.NOT_MODIFIED_304;
import static com.vodafone.global.sdk.http.HttpCode.OK_200;

public class ResolveGetRequest extends OkHttpSpiceRequest<UserDetailsDTO> {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final String url;
    private final String accessToken;
    private final UserDetailsDTO userDetailsDTO;
    private RequestBuilderProvider requestBuilderProvider;

    /**
     * Provides builder for {@link ResolveGetRequest}.
     */
    public static Builder builder() {
        return new Builder();
    }

    protected ResolveGetRequest(
            String url,
            String accessToken,
            UserDetailsDTO userDetailsDTO,
            RequestBuilderProvider requestBuilderProvider
    ) {
        super(UserDetailsDTO.class);
        this.url = url;
        this.accessToken = accessToken;
        this.userDetailsDTO = userDetailsDTO;
        this.requestBuilderProvider = requestBuilderProvider;
    }

    @Override
    public UserDetailsDTO loadDataFromNetwork() throws Exception {
        Request request = requestBuilderProvider.builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("etag", userDetailsDTO.etag)
                .get()
                .build();
        OkHttpClient client = getOkHttpClient();
        Response response = client.newCall(request).execute();
        int code = response.code();
        switch (code) {
            case OK_200:
                return parseJson(response);
            case NOT_MODIFIED_304:
                return userDetailsDTO;
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

    private UserDetailsDTO parseJson(Response response) throws IOException, JSONException {
        String jsonString = response.body().string();
        UserDetails userDetails = UserDetails.fromJson(jsonString, ResolutionStatus.FIXME);
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
        private UserDetailsDTO userDetailsDTO;
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

        public Builder userDetaildDTO(UserDetailsDTO userDetailsDTO) {
            this.userDetailsDTO = userDetailsDTO;
            return this;
        }

        public Builder requestBuilderProvider(RequestBuilderProvider requestBuilderProvider) {
            this.requestBuilderProvider = requestBuilderProvider;
            return this;
        }

        public ResolveGetRequest build() {
            return new ResolveGetRequest(url, accessToken, userDetailsDTO, requestBuilderProvider);
        }
    }
}
