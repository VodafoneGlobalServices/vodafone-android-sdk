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
    final HapSettings hap;
    final ApixSettings apix;

    public Settings(Context context) {
        JSONObject json = parseJSON(context);
        try {
            hap = new HapSettings(json.getJSONObject("HAP"));
            apix = new ApixSettings(json.getJSONObject("APIX"));
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
        return context.getResources().openRawResource(R.raw.config);
    }

    class HapSettings {
        final String protocol;
        final String host;
        final UserDetailsSettings userDetails;

        public HapSettings(JSONObject json) throws JSONException {
            protocol = json.getString("protocol");
            host = json.getString("host");
            userDetails = new UserDetailsSettings(json.getJSONObject("user_details"));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            HapSettings that = (HapSettings) o;

            if (!host.equals(that.host)) return false;
            if (!protocol.equals(that.protocol)) return false;
            if (!userDetails.equals(that.userDetails)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = protocol.hashCode();
            result = 31 * result + host.hashCode();
            result = 31 * result + userDetails.hashCode();
            return result;
        }

        class UserDetailsSettings {
            final String path;

            public UserDetailsSettings(JSONObject json) throws JSONException {
                path = json.getString("path");
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                UserDetailsSettings that = (UserDetailsSettings) o;

                if (!path.equals(that.path)) return false;

                return true;
            }

            @Override
            public int hashCode() {
                return path.hashCode();
            }
        }
    }

    class ApixSettings {
        final String protocol;
        final String host;
        final SmsValidationSettings smsValidation;

        public ApixSettings(JSONObject json) throws JSONException {
            protocol = json.getString("protocol");
            host = json.getString("host");
            smsValidation = new SmsValidationSettings(json.getJSONObject("sms_validation"));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ApixSettings that = (ApixSettings) o;

            if (!host.equals(that.host)) return false;
            if (!protocol.equals(that.protocol)) return false;
            if (!smsValidation.equals(that.smsValidation)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = protocol.hashCode();
            result = 31 * result + host.hashCode();
            result = 31 * result + smsValidation.hashCode();
            return result;
        }

        class SmsValidationSettings {
            final String path;

            public SmsValidationSettings(JSONObject json) throws JSONException {
                path = json.getString("path");
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                SmsValidationSettings that = (SmsValidationSettings) o;

                if (!path.equals(that.path)) return false;

                return true;
            }

            @Override
            public int hashCode() {
                return path.hashCode();
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Settings settings = (Settings) o;

        if (!apix.equals(settings.apix)) return false;
        if (!hap.equals(settings.hap)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = hap.hashCode();
        result = 31 * result + apix.hashCode();
        return result;
    }
}
