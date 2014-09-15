package com.vodafone.global.sdk;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class UserDetails {
    public final boolean resolved;
    public final boolean stillRunning;
    public final String token;
    public final Date expires;
    public final boolean validationRequired;
    public final String acr;

    public static Builder builder() {
        return new Builder();
    }

    protected UserDetails(boolean resolved, boolean stillRunning, String token, Date expires, boolean validationRequired, String acr) {
        this.resolved = resolved;
        this.stillRunning = stillRunning;
        this.token = token;
        this.expires = expires;
        this.validationRequired = validationRequired;
        this.acr = acr;
    }

    public static UserDetails fromJson(boolean resolved, boolean stillRunning, boolean validationRequired, String jsonString) throws JSONException {
        JSONObject json = new JSONObject(jsonString);
        String acr = json.getString("acr");
        String token = json.getString("token");
        int expiresIn = json.getInt("expiresIn"); //times in milliseconds
        long currentTime = System.currentTimeMillis();
        long expirationTime = currentTime + expiresIn;
        Date expires = new Date(expirationTime);
        return new UserDetails(resolved, stillRunning, token, expires, validationRequired, acr);
    }

    public static UserDetails fromJson(String jsonString) throws JSONException {
        JSONObject json = new JSONObject(jsonString);
        String acr = json.getString("acr");
        String token = json.getString("token");
        int expiresIn = json.getInt("expiresIn"); //times in milliseconds
        long currentTime = System.currentTimeMillis();
        long expirationTime = currentTime + expiresIn;
        Date expires = new Date(expirationTime);
        return new UserDetails(false, false, token, expires, false, acr);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserDetails that = (UserDetails) o;

        if (resolved != that.resolved) return false;
        if (stillRunning != that.stillRunning) return false;
        if (validationRequired != that.validationRequired) return false;
        if (expires != null ? !expires.equals(that.expires) : that.expires != null) return false;
        if (token != null ? !token.equals(that.token) : that.token != null) return false;
        if (acr != null ? !acr.equals(that.acr) : that.acr != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (resolved ? 1 : 0);
        result = 31 * result + (stillRunning ? 1 : 0);
        result = 31 * (token != null ? token.hashCode() : 0);
        result = 31 * result + (expires != null ? expires.hashCode() : 0);
        result = 31 * result + (validationRequired ? 1 : 0);
        result = 31 * result + (acr != null ? acr.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "UserDetails{" +
                "resolved=" + resolved +
                ", stillRunning=" + stillRunning +
                ", token='" + token + '\'' +
                ", expires=" + expires +
                ", validationRequired=" + validationRequired +
                ", acr=" + acr;
    }

    public static class Builder {
        public boolean resolved;
        public boolean stillRunning;
        public String token;
        public Date expires;
        public boolean validationRequired;
        public String acr;

        private Builder() {
        }

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder resolved(boolean resolved) {
            this.resolved = resolved;
            return this;
        }

        public Builder stillRunning(boolean stillRunning) {
            this.stillRunning = stillRunning;
            return this;
        }

        public Builder expires(Date expires) {
            this.expires = expires;
            return this;
        }

        public Builder validationRequired(boolean validated) {
            this.validationRequired = validated;
            return this;
        }

        public Builder acr(String acr) {
            this.acr = acr;
            return this;
        }

        public UserDetails build() {
            return new UserDetails(resolved, stillRunning, token, expires, validationRequired, acr);
        }
    }
}
