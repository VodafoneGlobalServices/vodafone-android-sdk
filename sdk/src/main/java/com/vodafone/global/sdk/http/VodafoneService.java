package com.vodafone.global.sdk.http;

import android.app.Application;

import com.octo.android.robospice.okhttp.OkHttpSpiceService;
import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.persistence.exception.CacheCreationException;

public class VodafoneService extends OkHttpSpiceService {
    @Override
    public void onCreate() {
        super.onCreate();
        getOkHttpClient().setFollowRedirects(false);
    }

    @Override
    public CacheManager createCacheManager(Application application) throws CacheCreationException {
        return new CacheManager();
    }
}
