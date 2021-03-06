package com.vodafone.global.sdk.http.resolve;

import com.vodafone.global.sdk.IMSI;
import com.vodafone.global.sdk.RequestBuilderProvider;
import com.vodafone.global.sdk.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class ResolvePostImsiRequest extends ResolvePostRequest {
    private final IMSI imsi;

    protected ResolvePostImsiRequest(
            String url,
            String accessToken,
            IMSI imsi,
            boolean smsValidation,
            RequestBuilderProvider requestBuilderProvider,
            Logger logger
    ) {
        super(url, accessToken, smsValidation, requestBuilderProvider, logger);
        this.imsi = imsi;
    }

    protected String prepareBody() throws JSONException {
        JSONObject json = new JSONObject();
        if (imsi.isPresent()) {
            json.put("imsi", imsi.get());
        }
        json.put("smsValidation", smsValidation);
        return json.toString();
    }
}
