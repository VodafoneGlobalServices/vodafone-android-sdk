package com.vodafone.global.sdk.http.settings;

import android.content.Context;
import android.content.SharedPreferences;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.vodafone.global.sdk.RequestBuilderProvider;
import com.vodafone.global.sdk.Settings;
import com.vodafone.global.sdk.http.ResponseHolder;
import com.vodafone.global.sdk.logging.LogUtil;
import com.vodafone.global.sdk.logging.Logger;
import org.json.JSONException;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.vodafone.global.sdk.http.HttpCode.OK_200;

public class UpdateSettingsProcessor {
    private final Context context;
    private RequestBuilderProvider requestBuilderProvider;
    private final Logger logger;

    public UpdateSettingsProcessor(
            Context context,
            RequestBuilderProvider requestBuilderProvider,
            Logger logger
    ) {
        this.context = context;
        this.requestBuilderProvider = requestBuilderProvider;
        this.logger = logger;
    }

    public Settings process() throws IOException, JSONException {
        SharedPreferences preferences = context.getSharedPreferences(Settings.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);

        Request request = requestBuilderProvider.builder()
                .addHeader("If-Modified-Since", preferences.getString(Settings.LAST_MODIFIED, ""))
                .addHeader("If-None-Match", preferences.getString(Settings.ETAG, ""))
                .url(Settings.CONFIG_URL)
                .get()
                .build();

        logger.d(LogUtil.prepareRequestLogMsg(request));
        OkHttpClient client = new OkHttpClient();
        Response response = client.newCall(request).execute();
        ResponseHolder responseHolder = new ResponseHolder(response);
        logger.d(LogUtil.prepareResponseLogMsg(responseHolder));

        int code = responseHolder.code();
        switch (code) {
            case OK_200:
                String lastModified = responseHolder.header("Last-Modified");
                String etag = responseHolder.header("Etag");
                String cacheControl = responseHolder.header("Cache-Control");
                Pattern pattern = Pattern.compile(".*max-age=(\\d+).*");
                Matcher matcher = pattern.matcher(cacheControl);
                if (!matcher.matches()) throw new IllegalStateException("Can't extract max-age");
                String group = matcher.group(1);
                Integer maxAgeInMiliSeconds = Integer.valueOf(group) * 1000;
                long expiresAt = System.currentTimeMillis() + maxAgeInMiliSeconds;
                String json = responseHolder.body();
                Settings settings = new Settings(json);

                preferences.edit()
                        .putString(Settings.LAST_MODIFIED, lastModified)
                        .putString(Settings.ETAG, etag)
                        .putLong(Settings.EXPIREST_AT, expiresAt)
                        .putString(Settings.SETTINGS_JSON, json)
                        .apply();

                return settings;
            default:
                return null;
        }
    }
}
