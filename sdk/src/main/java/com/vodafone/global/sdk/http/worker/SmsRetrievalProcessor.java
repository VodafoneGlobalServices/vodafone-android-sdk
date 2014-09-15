package com.vodafone.global.sdk.http.worker;

import android.content.Context;
import android.os.Message;

import com.google.common.base.Optional;
import com.vodafone.global.sdk.Settings;
import com.vodafone.global.sdk.UserDetailsCallback;
import com.vodafone.global.sdk.http.oauth.OAuthToken;

import java.util.Set;

public class SmsRetrievalProcessor extends RequestProcessor {
    public SmsRetrievalProcessor(Context context, Settings settings, Set<UserDetailsCallback> userDetailsCallback) {
        super(context, settings, userDetailsCallback);
    }

    void process(Worker worker, Optional<OAuthToken> authToken, Message msg) {
    }
}
