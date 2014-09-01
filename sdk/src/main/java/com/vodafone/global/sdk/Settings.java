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

class Settings {
    final PathSettings oauth;
    final PathSettings resolveOverWiFi;
    final PathSettings resolveOverMobile;
    final PathSettings checkStatus;
    final PathSettings validatePin;

    public Settings(Context context) {
        JSONObject json = parseJSON(context);
        try {
            oauth = new PathSettings(json.getJSONObject("oauth"));
            resolveOverWiFi = new PathSettings(json.getJSONObject("resolve over wifi"));
            resolveOverMobile = new PathSettings(json.getJSONObject("resolve over mobile"));
            checkStatus = new PathSettings(json.getJSONObject("check status"));
            validatePin = new PathSettings(json.getJSONObject("validate pin"));
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
