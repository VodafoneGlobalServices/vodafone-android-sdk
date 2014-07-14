package com.vodafone.global.sdk;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.List;

class UserDetailsResponseCallback implements Callback {
    private List<UserDetailsCallback> userDetailsCallbacks;

    public UserDetailsResponseCallback(List<UserDetailsCallback> userDetailsCallbacks) {
        this.userDetailsCallbacks = userDetailsCallbacks;
    }

    @Override
    public void onFailure(Request request, IOException e) {
        for (UserDetailsCallback callback : userDetailsCallbacks) {
            callback.onUserDetailsError(new VodafoneException(e.getMessage(), e));
        }
    }

    @Override
    public void onResponse(Response response) throws IOException {
        int httpCode = response.code();
        String httpBody = response.body().string();

        UserDetails userDetails = null; // TODO httpBody json -> UserDetails

        for (UserDetailsCallback callback : userDetailsCallbacks) {
            callback.onUserDetailsUpdate(userDetails);
        }
    }
}
