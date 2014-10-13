package com.vodafone.global.sdk.http.resolve;

import com.vodafone.global.sdk.MSISDN;
import com.vodafone.global.sdk.RequestBuilderProvider;
import com.vodafone.global.sdk.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class ResolvePostMsisdnRequest extends ResolvePostRequest {
    private final MSISDN msisdn;

    protected ResolvePostMsisdnRequest(
            String url,
            String accessToken,
            MSISDN msisdn,
            boolean smsValidation,
            RequestBuilderProvider requestBuilderProvider,
            Logger logger
    ) {
        super(url, accessToken, smsValidation, requestBuilderProvider, logger);
        this.msisdn = msisdn;
    }

    protected String prepareBody() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("msisdn", msisdn.get());
        json.put("market", msisdn.marketCode());
        json.put("smsValidation", smsValidation);
        return json.toString();
    }
}
