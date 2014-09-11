package com.vodafone.global.sdk;

import android.content.Context;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Settings {
    public final PathSettings oauth;
    public final PathSettings resolveOverWiFi;
    public final PathSettings resolveOverMobile;
    public final PathSettings checkStatus;
    public final PathSettings validatePin;

    public final long configurationUpdateCheckTimeSpan;   //Time interval (in seconds) - time to wait until next configuration update can run.
    public final long defaultHttpConnectionTimeout;       //Http connection time out.
    public final long requestsThrottlingLimit;            //Number of maximum requests which can be performed in specified time period.
    public final long requestsThrottlingPeriod;           //Time period for requestsThrottlingLimit, time period in seconds.
    public final String oAuthTokenClientId;               //Client id for oAuthToken retrieval.
    public final String oAuthTokenClientSecret;           //Client secret for oAuthToken retrieval.
    public final String oAuthTokenScope;                  //Scope for oAuthToken retrieval.
    public final String imsiValidationRegex;              //Regular expression for imsi validation.
    public final String oAuthTokenGrantType;

    public final String sdkId = "ABCDEF";

    public Settings(Context context) {
        JSONObject json = parseJSON(context);
        try {
            oauth = new PathSettings(json.getJSONObject("oauth"));
            resolveOverWiFi = new PathSettings(json.getJSONObject("resolve over wifi"));
            resolveOverMobile = new PathSettings(json.getJSONObject("resolve over mobile"));
            checkStatus = new PathSettings(json.getJSONObject("check status"));
            validatePin = new PathSettings(json.getJSONObject("validate pin"));
            configurationUpdateCheckTimeSpan = json.getInt("configurationUpdateCheckTimeSpan");
            defaultHttpConnectionTimeout = json.getInt("defaultHttpConnectionTimeout");
            requestsThrottlingLimit = json.getInt("requestsThrottlingLimit");
            requestsThrottlingPeriod = json.getInt("requestsThrottlingPeriod");
            oAuthTokenClientId = json.getString("oAuthTokenClientId");
            oAuthTokenClientSecret = json.getString("oAuthTokenClientSecret");
            oAuthTokenScope = json.getString("oAuthTokenScope");
            oAuthTokenGrantType = json.getString("oAuthTokenGrantType");
            imsiValidationRegex = json.getString("imsiValidationRegex");
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }
    }

    private JSONObject parseJSON(Context context) {
        String jsonConfig = readConfig(context);
        try {
            return new JSONObject(jsonConfig);
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }
    }

    private String readConfig(Context context) {
        InputStream is = openConfigFile(context);
        try {
            return CharStreams.toString(new InputStreamReader(is, Charsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } finally {
            Closeables.closeQuietly(is);
        }
    }

    private InputStream openConfigFile(Context context) {
        try {
            return context.getAssets().open("config.json");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public class PathSettings {
        public final String protocol;
        public final String host;
        public final String path;

        PathSettings(JSONObject json) throws JSONException {
            protocol = json.getString("protocol");
            host = json.getString("host");
            path = json.getString("path");
        }
    }
}
