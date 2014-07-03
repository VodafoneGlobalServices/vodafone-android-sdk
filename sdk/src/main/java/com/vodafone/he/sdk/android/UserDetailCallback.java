package com.vodafone.he.sdk.android;

public interface UserDetailCallback {
    void onSuccess(UserDetails userDetails);
    void onError(VodafoneException ex);
}
