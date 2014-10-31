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
import java.util.*;

public class Settings {
    public final PathSettings apix;
    public final PathSettings hap;
    public final PathSettings oauth;

    public final long defaultHttpConnectionTimeout;
    public final long requestsThrottlingLimit;
    public final long requestsThrottlingPeriod;
    public final String oAuthTokenScope;
    public final String phoneNumberRegex;
    public final String smsInterceptionRegex;
    public final String oAuthTokenGrantType;
    public final String sdkId = "VFSeamlessID SDK/Android (v1.0.0)";
    public List<String> availableMccMnc;
    public final Map<String, String> availableMarkets;

    public Settings(Context context) {
        JSONObject json = parseJSON(context);
        try {
            apix = readApix(json);
            hap = readHap(json);
            oauth = readOAuth(json);
            defaultHttpConnectionTimeout = json.getInt("defaultHttpConnectionTimeout");
            requestsThrottlingLimit = json.getInt("requestsThrottlingLimit");
            requestsThrottlingPeriod = json.getInt("requestsThrottlingPeriod");
            JSONObject apix = json.getJSONObject("apix");
            oAuthTokenScope = apix.getString("oAuthTokenScope");
            oAuthTokenGrantType = apix.getString("oAuthTokenGrantType");
            phoneNumberRegex = json.getString("phoneNumberRegex");
            availableMccMnc = getAvailableMccMnc(json);
            availableMarkets = getAvailableMarkets(json);
            smsInterceptionRegex = json.getString("smsInterceptionRegex");
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }
    }

    private PathSettings readOAuth(JSONObject json) throws JSONException {
        JSONObject apix = json.getJSONObject("apix");
        String protocol = apix.getString("protocol");
        String host = apix.getString("host");
        String path = apix.getString("oAuthTokenPath");
        return new PathSettings(protocol, host, path);
    }

    private PathSettings readHap(JSONObject json) throws JSONException {
        JSONObject hap = json.getJSONObject("hap");
        String protocol = BuildConfig.DIRECT
                ? BuildConfig.DIRECT_PROTOCOL
                : hap.getString("protocol");
        String host = BuildConfig.DIRECT
                ? BuildConfig.DIRECT_HOST
                : hap.getString("host");
        String path = json.getString("basePath");
        return new PathSettings(protocol, host, path);
    }

    private PathSettings readApix(JSONObject json) throws JSONException {
        JSONObject apix = json.getJSONObject("apix");
        String protocol = BuildConfig.DIRECT
                ? BuildConfig.DIRECT_PROTOCOL
                : apix.getString("protocol");
        String host = BuildConfig.DIRECT
                ? BuildConfig.DIRECT_HOST
                : apix.getString("host");
        String path = json.getString("basePath");
        return new PathSettings(protocol, host, path);
    }

    private List<String> getAvailableMccMnc(JSONObject json) throws JSONException {
        ArrayList<String> mccMnc = new ArrayList<String>();
        JSONArray array = json.getJSONArray("availableMccMnc");
        for (int i = 0, length = array.length(); i < length; i++) {
            mccMnc.add(array.getString(i));
        }
        return mccMnc;
    }

    private Map<String, String> getAvailableMarkets(JSONObject json) throws JSONException {
        JSONObject markets = json.getJSONObject("availableMarkets");

        ArrayList<String> keys = new ArrayList<String>();

        Iterator iterator = markets.keys();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            keys.add(key);
        }

        HashMap<String, String> availableMarkets = new HashMap<String, String>();
        for (String marketCode : keys) {
            String marketNumber = String.valueOf(markets.getInt(marketCode));
            availableMarkets.put(marketNumber, marketCode);
        }

        return availableMarkets;
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

        PathSettings(String protocol, String host, String path) {
            this.protocol = protocol;
            this.host = host;
            this.path = path;
        }
    }
}
