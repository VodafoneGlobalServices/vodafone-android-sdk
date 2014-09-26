package com.vodafone.global.sdk;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class UserDetails {
    public static final UserDetails RESOLUTION_FAILED = new UserDetails(ResolutionStatus.FAILED, "", new Date(), "");
    public final ResolutionStatus status;
    public final String token;
    public final Date expires;
    public final String acr;

    public static Builder builder() {
        return new Builder();
    }

    protected UserDetails(ResolutionStatus status, String token, Date expires, String acr) {
        this.status = status;
        this.token = token;
        this.expires = expires;
        this.acr = acr;
    }

    public static UserDetails fromJson(String jsonString, ResolutionStatus status) throws JSONException {
        JSONObject json = new JSONObject(jsonString);
        String acr = json.getString("acr");
        String token = json.getString("token");
        int expiresIn = json.getInt("expiresIn"); //times in milliseconds
        long currentTime = System.currentTimeMillis();
        long expirationTime = currentTime + expiresIn;
        Date expires = new Date(expirationTime);
        return new UserDetails(status, token, expires, acr);
    }

    public static UserDetails validationRequired(String token) {
        return new UserDetails(ResolutionStatus.VALIDATION_REQUIRED, token, new Date(), "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserDetails that = (UserDetails) o;

        if (!acr.equals(that.acr)) return false;
        if (!expires.equals(that.expires)) return false;
        if (status != that.status) return false;
        if (!token.equals(that.token)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = status.hashCode();
        result = 31 * result + token.hashCode();
        result = 31 * result + expires.hashCode();
        result = 31 * result + acr.hashCode();
        return result;
    }

    public static class Builder {
        public ResolutionStatus status;
        public String token;
        public Date expires;
        public String acr;

        private Builder() {
        }

        public void status(ResolutionStatus status) {
            this.status = status;
        }

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder expires(Date expires) {
            this.expires = expires;
            return this;
        }

        public Builder acr(String acr) {
            this.acr = acr;
            return this;
        }

        public UserDetails build() {
            return new UserDetails(status, token, expires, acr);
        }
    }
}
