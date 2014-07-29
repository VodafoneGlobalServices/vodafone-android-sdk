package com.vodafone.global.sdk;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import timber.log.Timber;

import java.io.IOException;
import java.util.Set;

class UserDetailsResponseCallback implements Callback {
    private Set<UserDetailsCallback> userDetailsCallbacks;

    public UserDetailsResponseCallback(Set<UserDetailsCallback> userDetailsCallbacks) {
        this.userDetailsCallbacks = userDetailsCallbacks;
    }

    @Override
    public void onFailure(Request request, IOException e) {
        Timber.e(e, e.getMessage());

        for (UserDetailsCallback callback : userDetailsCallbacks) {
            callback.onUserDetailsError(new VodafoneException(e.getMessage(), e));
        }
    }

    @Override
    public void onResponse(Response response) throws IOException {
        int httpCode = response.code();

        switch (httpCode) {
            case HttpStatus.SC_OK:
                try {
                    String httpBody = response.body().string();
                    UserDetails userDetails = UserDetails.fromJson(httpBody);
                    for (UserDetailsCallback callback : userDetailsCallbacks) {
                        callback.onUserDetailsUpdate(userDetails);
                    }
                } catch (JSONException e) {
                    Timber.e(e, e.getMessage());
                }
                break;
            default:
                // TODO server failure, should not happen, need to decide about handling
        }
    }
}
