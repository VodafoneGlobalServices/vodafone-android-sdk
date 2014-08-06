package com.vodafone.global.sdk;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class UserDetails {
    public final boolean resolved;
    public final boolean stillRunning;
    public final String source;
    public final String token;
    public final boolean tetheringConflict;
    public final boolean secure;
    public final Date expires;
    public final boolean validated;

    public UserDetails(
            boolean resolved, boolean stillRunning, String source, String token,
            boolean tetheringConflict, boolean secure, Date expires, boolean validated
    ) {
        this.resolved = resolved;
        this.stillRunning = stillRunning;
        this.source = source;
        this.token = token;
        this.tetheringConflict = tetheringConflict;
        this.secure = secure;
        this.expires = expires;
        this.validated = validated;
    }

    public static UserDetails fromJson(String jsonString) throws JSONException {
        JSONObject json = new JSONObject(jsonString);

        boolean resolved = json.getBoolean("resolved");
        boolean stillRunning = json.getBoolean("stillRunning");
        String source = json.getString("source");
        String token = json.getString("token");
        boolean tetheringConflict = json.getBoolean("tetheringConflict");
        boolean secure = json.getBoolean("secure");
        String date = json.getString("expires");
        DateTimeFormatter parser2 = ISODateTimeFormat.dateTimeNoMillis();
        Date expires = parser2.parseDateTime(date).toDate();
        boolean validated = json.getBoolean("validated");

        return new UserDetails(resolved, stillRunning, source, token, tetheringConflict, secure,
                expires, validated);
    }
}
