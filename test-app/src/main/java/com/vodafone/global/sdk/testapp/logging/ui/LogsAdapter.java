package com.vodafone.global.sdk.testapp.logging.ui;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.vodafone.global.sdk.testapp.R;
import com.vodafone.global.sdk.testapp.logging.database.LogColumns;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

public class LogsAdapter extends CursorAdapter {
    public static final int WARN_COLOR = Color.parseColor("#FCE94F");
    public static final int DEBUG_COLOR = Color.parseColor("#729FCF");
    public static final int ERROR_COLOR = Color.parseColor("#FF6B68");
    public static final int INFO_COLOR = Color.parseColor("#8AE234");
    public static final int DEFAULT_COLOR = Color.WHITE;

    public LogsAdapter(Context context, Cursor cursor) {
        super(context, cursor, false);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_log, parent, false);
        view.setTag(new ViewHolder(view));
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        String timestampString = cursor.getString(cursor.getColumnIndex(LogColumns.TIMESTAMP));
        DateTime timestamp = ISODateTimeFormat.dateTime().parseDateTime(timestampString);
        int level = cursor.getInt(cursor.getColumnIndex(LogColumns.LEVEL));
        viewHolder.logMsg.setTextColor(createColor(level));
        String logMsg = cursor.getString(cursor.getColumnIndex(LogColumns.MSG));
        viewHolder.logMsg.setText(logMsg);
    }

    private int createColor(int logLevel) {
        switch (logLevel) {
            case Log.INFO: return INFO_COLOR;
            case Log.WARN: return WARN_COLOR;
            case Log.DEBUG: return DEBUG_COLOR;
            case Log.ERROR: return ERROR_COLOR;
            default: return DEFAULT_COLOR;
        }
    }

    static class ViewHolder {
        @InjectView(R.id.log_msg) TextView logMsg;

        ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
