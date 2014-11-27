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
    public static final String CONFIG_URL = BuildConfig.CONFIG_URL;
    public static final String LAST_MODIFIED = "Last-Modified";
    public static final String ETAG = "Etag";
    public static final String SETTINGS_JSON = "SettingsJSON";
    public static final String SHARED_PREFERENCES_NAME = "Vodafone";
    public static final String EXPIREST_AT = "ExpiresAt";

    private PathSettings apix;
    private PathSettings hap;
    private PathSettings oauth;

    private long defaultHttpConnectionTimeout;
    private long requestsThrottlingLimit;
    private long requestsThrottlingPeriod;
    private String oAuthTokenScope;
    private String phoneNumberRegex;
    private String smsInterceptionRegex;
    private String oAuthTokenGrantType;
    private final static String sdkId = "VFSeamlessID SDK/Android (v1.0.0)";
    private List<String> availableMccMnc;
    private Map<String, String> availableMarkets;
    private int smsValidationTimeoutInSeconds;

    public Settings(Context context) {
        JSONObject json = parseJSON(context);
        setFields(json);
    }

    public Settings(String json) throws JSONException {
        setFields(new JSONObject(json));
    }

    private void setFields(JSONObject json) {
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
            availableMccMnc = Collections.unmodifiableList(getAvailableMccMnc(json));
            availableMarkets = Collections.unmodifiableMap(getAvailableMarkets(json));
            smsInterceptionRegex = json.getString("smsInterceptionRegex");
            smsValidationTimeoutInSeconds = json.getInt("smsValidationTimeoutInSeconds");
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

    public PathSettings apix() {
        return apix;
    }

    public PathSettings hap() {
        return hap;
    }

    public PathSettings oauth() {
        return oauth;
    }

    public long defaultHttpConnectionTimeout() {
        return defaultHttpConnectionTimeout;
    }

    public long requestsThrottlingLimit() {
        return requestsThrottlingLimit;
    }

    public long requestsThrottlingPeriod() {
        return requestsThrottlingPeriod;
    }

    public String oAuthTokenScope() {
        return oAuthTokenScope;
    }

    public String phoneNumberRegex() {
        return phoneNumberRegex;
    }

    public String smsInterceptionRegex() {
        return smsInterceptionRegex;
    }

    public String oAuthTokenGrantType() {
        return oAuthTokenGrantType;
    }

    public String sdkId() {
        return sdkId;
    }

    public List<String> availableMccMnc() {
        return availableMccMnc;
    }

    public Map<String, String> availableMarkets() {
        return availableMarkets;
    }

    public String pinRegex() {
        return "^[0-9]{4}$";
    }

    public int smsValidationTimeoutInSeconds() {
        return smsValidationTimeoutInSeconds;
    }

    public class PathSettings {
        private final String protocol;
        private final String host;
        private final String path;

        PathSettings(String protocol, String host, String path) {
            this.protocol = protocol;
            this.host = host;
            this.path = path;
        }

        public String protocol() {
            return protocol;
        }

        public String host() {
            return host;
        }

        public String path() {
            return path;
        }
    }
}
