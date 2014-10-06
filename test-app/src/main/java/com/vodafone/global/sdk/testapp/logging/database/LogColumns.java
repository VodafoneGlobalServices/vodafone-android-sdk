package com.vodafone.global.sdk.testapp.logging.database;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;

public interface LogColumns {
    @DataType(DataType.Type.INTEGER)
    @PrimaryKey
    @AutoIncrement
    String _ID = "_id";

    @DataType(DataType.Type.TEXT)
    @NotNull
    String TIMESTAMP = "timestamp";

    @DataType(DataType.Type.INTEGER)
    @NotNull
    String LEVEL = "level";

    @DataType(DataType.Type.TEXT)
    @NotNull
    String TAG = "tag";

    @DataType(DataType.Type.TEXT)
    @NotNull
    String MSG = "msg";
}
