package com.vodafone.global.sdk;

import android.content.Context;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Settings {
    public final PathSettings apix;
    public final PathSettings hap;
    public final PathSettings oauth;

    public final long configurationUpdateCheckTimeSpan;   //Time interval (in seconds) - time to wait until next configuration update can run.
    public final long defaultHttpConnectionTimeout;       //Http connection time out.
    public final long requestsThrottlingLimit;            //Number of maximum requests which can be performed in specified time period.
    public final long requestsThrottlingPeriod;           //Time period for requestsThrottlingLimit, time period in seconds.l.
    public final String oAuthTokenScope;                  //Scope for oAuthToken retrieval.
    public final String msisdnValidationRegex;              //Regular expression for imsi validation.
    public final String oAuthTokenGrantType;

    public final String sdkId = "VFSeamlessID SDK/Android (v1.0.0)";
    public List<String> availableMccMnc;

    public Settings(Context context) {
        JSONObject json = parseJSON(context);
        try {
            apix = new PathSettings(json.getJSONObject("apix"));
            hap = new PathSettings(json.getJSONObject("hap"));
            oauth = new PathSettings(json.getJSONObject("oauth"));
            configurationUpdateCheckTimeSpan = json.getInt("configurationUpdateCheckTimeSpan");
            defaultHttpConnectionTimeout = json.getInt("defaultHttpConnectionTimeout");
            requestsThrottlingLimit = json.getInt("requestsThrottlingLimit");
            requestsThrottlingPeriod = json.getInt("requestsThrottlingPeriod");
            oAuthTokenScope = json.getString("oAuthTokenScope");
            oAuthTokenGrantType = json.getString("oAuthTokenGrantType");
            msisdnValidationRegex = json.getString("msisdnValidationRegex");
            availableMccMnc = getAvailableMccMnc(json);
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }
    }

    private List<String> getAvailableMccMnc(JSONObject json) throws JSONException {
        ArrayList<String> mccMnc = new ArrayList<String>();
        JSONArray array = json.getJSONArray("availableMccMnc");
        for (int i = 0, length = array.length(); i < length; i++) {
            mccMnc.add(array.getString(i));
        }
        return mccMnc;
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
