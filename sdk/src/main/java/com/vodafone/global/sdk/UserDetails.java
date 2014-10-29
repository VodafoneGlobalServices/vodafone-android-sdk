package com.vodafone.global.sdk;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class UserDetails {
    public final String tokenId;
    public final Date expires;
    public final String acr;

    public static Builder builder() {
        return new Builder();
    }

    protected UserDetails(String tokenId, Date expires, String acr) {
        this.tokenId = tokenId;
        this.expires = expires;
        this.acr = acr;
    }

    public static UserDetails fromJson(String jsonString) throws JSONException {
        JSONObject json = new JSONObject(jsonString);
        String acr = json.getString("acr");
        String tokenId = json.getString("tokenId");
        int expiresIn = json.getInt("expiresIn"); //times in milliseconds
        long currentTime = System.currentTimeMillis();
        long expirationTime = currentTime + expiresIn;
        Date expires = new Date(expirationTime);
        return new UserDetails(tokenId, expires, acr);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserDetails that = (UserDetails) o;

        if (!acr.equals(that.acr)) return false;
        if (!expires.equals(that.expires)) return false;
        if (!tokenId.equals(that.tokenId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = tokenId.hashCode();
        result = 31 * result + expires.hashCode();
        result = 31 * result + acr.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "UserDetails{" +
                "tokenId='" + tokenId + '\'' +
                ", expires=" + expires +
                ", acr='" + acr + '\'' +
                '}';
    }

    public static class Builder {
        public String token;
        public Date expires;
        public String acr;

        private Builder() {
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
            return new UserDetails(token, expires, acr);
        }
    }
}
