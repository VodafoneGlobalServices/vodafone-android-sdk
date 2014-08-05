package com.vodafone.global.sdk;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class UserDetails {
    private boolean resolved;
    private boolean stillRunning;
    private String source;
    private String token;
    private boolean tetheringConflict;
    private boolean secure;
    private Date expires;
    private boolean validated;

    public boolean getResolved() {
        return resolved;
    }

    public boolean getStillRunning() {
        return stillRunning;
    }

    public String getSource() {
        return source;
    }

    public String getToken() {
        return token;
    }

    public boolean getTetheringConflict() {
        return tetheringConflict;
    }

    public boolean getSecure() {
        return secure;
    }

    public Date getExpires() {
        return expires;
    }

    public boolean getValidated() {
        return validated;
    }

    public static UserDetails fromJson(String jsonString) throws JSONException {
        JSONObject json = new JSONObject(jsonString);

        UserDetails userDetails = new UserDetails();
        userDetails.resolved = json.getBoolean("resolved");
        userDetails.stillRunning = json.getBoolean("stillRunning");
        userDetails.source = json.getString("source");
        userDetails.token = json.getString("token");
        userDetails.tetheringConflict = json.getBoolean("tetheringConflict");
        userDetails.secure = json.getBoolean("secure");
        String date = json.getString("expires");
        DateTimeFormatter parser2 = ISODateTimeFormat.dateTimeNoMillis();
        userDetails.expires = parser2.parseDateTime(date).toDate();
        userDetails.validated = json.getBoolean("validated");

        return userDetails;
    }
}
