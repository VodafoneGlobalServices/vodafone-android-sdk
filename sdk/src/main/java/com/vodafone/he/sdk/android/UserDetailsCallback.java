package com.vodafone.he.sdk.android;

public interface UserDetailsCallback {
    void onSuccess(UserDetails userDetails);
    void onError(VodafoneException ex);
}
