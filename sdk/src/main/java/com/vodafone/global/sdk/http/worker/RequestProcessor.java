package com.vodafone.global.sdk.http.worker;

import android.content.Context;
import android.os.Message;
import com.google.common.base.Optional;
import com.vodafone.global.sdk.ResolveCallbacks;
import com.vodafone.global.sdk.Settings;
import com.vodafone.global.sdk.http.oauth.OAuthToken;

public abstract class RequestProcessor {
    protected final Worker worker;
    protected final Settings settings;
    protected final Context context;
    protected final ResolveCallbacks resolveCallbacks;

    public RequestProcessor(Context context, Worker worker, Settings settings, ResolveCallbacks resolveCallback) {
        this.context = context;
        this.worker = worker;
        this.settings = settings;
        this.resolveCallbacks = resolveCallback;
    }

    abstract void process(Optional<OAuthToken> authToken, Message msg);
}
