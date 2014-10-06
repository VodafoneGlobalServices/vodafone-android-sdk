package com.vodafone.global.sdk.testapp.logging.database;

import android.net.Uri;
import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

@ContentProvider(authority = LogsProvider.AUTHORITY, database = LogDatabase.class)
public final class LogsProvider {

    private LogsProvider() {
    }

    public static final String AUTHORITY = "com.vodafone.global.sdk.testapp.LogsProvider";
    static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    interface Path {
        String LOGS = "logs";
    }

    private static Uri buildUri(String... paths) {
        Uri.Builder builder = BASE_CONTENT_URI.buildUpon();
        for (String path : paths) {
            builder.appendPath(path);
        }
        return builder.build();
    }

    @TableEndpoint(table = LogDatabase.LOGS)
    public static class Logs {

        @ContentUri(
                path = Path.LOGS,
                type = "vnd.android.cursor.dir/log"
        )
        public static final Uri LOGS = buildUri(Path.LOGS);
    }
}
