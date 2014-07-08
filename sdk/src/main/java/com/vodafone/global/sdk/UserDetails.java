package com.vodafone.global.sdk;

public class UserDetails {
    private boolean resolved;
    private boolean stillRunning;
    private String source;
    private String token;
    private boolean tetheringConflict;
    private boolean secure;

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
}
