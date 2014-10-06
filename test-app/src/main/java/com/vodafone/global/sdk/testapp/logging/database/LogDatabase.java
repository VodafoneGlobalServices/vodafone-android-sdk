package com.vodafone.global.sdk.testapp.logging.database;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.Table;

@Database(version = LogDatabase.VERSION)
public final class LogDatabase {
    public static final int VERSION = 1;

    @Table(LogColumns.class)
    public static final String LOGS = "logs";
}
