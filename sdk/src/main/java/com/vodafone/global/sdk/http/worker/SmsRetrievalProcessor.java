package com.vodafone.global.sdk.http.worker;

import android.content.Context;
import android.os.Message;

import com.google.common.base.Optional;
import com.vodafone.global.sdk.Settings;
import com.vodafone.global.sdk.ResolveCallback;
import com.vodafone.global.sdk.http.oauth.OAuthToken;

import java.util.Set;

public class SmsRetrievalProcessor extends RequestProcessor {
    public SmsRetrievalProcessor(Context context, Worker worker, Settings settings, Set<ResolveCallback> resolveCallback) {
        super(context, worker, settings, resolveCallback);
    }

    @Override
    void process(Optional<OAuthToken> authToken, Message msg) {
    }
}
