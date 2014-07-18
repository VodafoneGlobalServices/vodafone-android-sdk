package com.vodafone.global.sdk;

import android.util.Log;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import org.json.JSONException;

import java.io.IOException;
import java.util.List;

class UserDetailsResponseCallback implements Callback {
    private List<UserDetailsCallback> userDetailsCallbacks;

    public UserDetailsResponseCallback(List<UserDetailsCallback> userDetailsCallbacks) {
        this.userDetailsCallbacks = userDetailsCallbacks;
    }

    @Override
    public void onFailure(Request request, IOException e) {
        Log.e("Vodafone", e.getMessage(), e);

        for (UserDetailsCallback callback : userDetailsCallbacks) {
            callback.onUserDetailsError(new VodafoneException(e.getMessage(), e));
        }
    }

    @Override
    public void onResponse(Response response) throws IOException {
        int httpCode = response.code();

        switch (httpCode) {
            case 200:
                try {
                    String httpBody = response.body().string();
                    UserDetails userDetails = UserDetails.fromJson(httpBody);
                    for (UserDetailsCallback callback : userDetailsCallbacks) {
                        callback.onUserDetailsUpdate(userDetails);
                    }
                } catch (JSONException e) {
                    Log.e("Vodafone", e.getMessage(), e);
                }
                break;
            default:
                // TODO server failure, should not happen, need to decide about handling
        }
    }
}
