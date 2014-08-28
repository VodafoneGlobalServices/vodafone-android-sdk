package com.vodafone.global.sdk;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class UserDetails {
    public final boolean resolved;
    /**
     * If true it means that the process is still running on server.
     */
    public final boolean stillRunning;
    public final String token;
    public final Date expires;
    public final boolean validationRequired;

    public static Builder builder() {
        return new Builder();
    }

    protected UserDetails(
            boolean resolved, boolean stillRunning, String token,
            Date expires, boolean validationRequired
    ) {
        this.resolved = resolved;
        this.stillRunning = stillRunning;
        this.token = token;
        this.expires = expires;
        this.validationRequired = validationRequired;
    }

    public static UserDetails fromJson(String jsonString) throws JSONException {
        JSONObject json = new JSONObject(jsonString);

        boolean resolved = json.getBoolean("resolved");
        boolean stillRunning = json.getBoolean("stillRunning");
        String token = json.getString("token");
        String date = json.getString("expires");
        DateTimeFormatter parser2 = ISODateTimeFormat.dateTimeNoMillis();
        Date expires = parser2.parseDateTime(date).toDate();
        boolean validationRequired = json.getBoolean("validationRequired");

        return new UserDetails(resolved, stillRunning, token,
                expires, validationRequired);
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

        return true;
    }

    @Override
    public int hashCode() {
        int result = (resolved ? 1 : 0);
        result = 31 * result + (stillRunning ? 1 : 0);
        result = 31 * result + (token != null ? token.hashCode() : 0);
        result = 31 * result + (expires != null ? expires.hashCode() : 0);
        result = 31 * result + (validationRequired ? 1 : 0);
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
                '}';
    }

    public static class Builder {
        public boolean resolved;
        public boolean stillRunning;
        public String token;
        public Date expires;
        public boolean validationRequired;

        private Builder() {
        }

        public Builder resolved(boolean resolved) {
            this.resolved = resolved;
            return this;
        }

        public Builder stillRunning(boolean stillRunning) {
            this.stillRunning = stillRunning;
            return this;
        }

        public Builder token(String token) {
            this.token = token;
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

        public UserDetails build() {
            return new UserDetails(resolved, stillRunning, token, expires, validationRequired);
        }
    }
}
