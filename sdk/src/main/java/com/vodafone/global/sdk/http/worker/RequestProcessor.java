package com.vodafone.global.sdk.http.worker;

import android.content.Context;
import android.os.Message;

import com.google.common.base.Optional;
import com.vodafone.global.sdk.Settings;
import com.vodafone.global.sdk.UserDetailsCallback;
import com.vodafone.global.sdk.VodafoneException;
import com.vodafone.global.sdk.http.oauth.OAuthToken;
import com.vodafone.global.sdk.http.resolve.UserDetailsDTO;

import java.util.Set;

/**
 * Created by bamik on 2014-09-10.
 */
public abstract class RequestProcessor {
    protected final Settings settings;
    protected final Context context;
    private final Set<UserDetailsCallback> userDetailsCallbacks;


    public RequestProcessor(Context context, Settings settings, Set<UserDetailsCallback> userDetailsCallback) {
        this.context = context;
        this.settings = settings;
        this.userDetailsCallbacks = userDetailsCallback;
    }

    abstract void process(Worker worker, Optional<OAuthToken> authToken, Message msg);

    protected void notifyUserDetailUpdate(UserDetailsDTO userDetailsDTO) {
        for (UserDetailsCallback callback : userDetailsCallbacks)
            callback.onUserDetailsUpdate(userDetailsDTO.userDetails);
    }

    protected void notifyError(VodafoneException vodafoneException) {
        for (UserDetailsCallback callback : userDetailsCallbacks)
            callback.onUserDetailsError(vodafoneException);
    }
}
