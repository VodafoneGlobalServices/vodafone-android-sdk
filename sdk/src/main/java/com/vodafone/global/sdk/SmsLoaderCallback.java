package com.vodafone.global.sdk;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import com.google.common.base.Optional;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SmsLoaderCallback implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "LoaderCallback";
    private final Context context;
    private final Pattern pattern;

    SmsLoaderCallback(Context context, String regex) {
        this.context = context;
        pattern = Pattern.compile(regex);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader(int, Bundle)");
        return new CursorLoader(context, Uri.parse("content://sms/"), null, null, null, "date ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished(Loader<Cursor>, Cursor)");


        for (int columnIndex = 0, columnCount = data.getColumnCount(); columnIndex < columnCount; columnIndex++) {
            String columnName = data.getColumnName(columnIndex);
            Log.d(TAG, "col[" + columnIndex + "] = " + columnName);
        }

        ArrayList<Sms> smses = new ArrayList<Sms>();
        while (data.moveToNext()) {
            Sms sms = new Sms(data);
            smses.add(sms);
            Log.d(TAG, sms.toString());
        }

        ArrayList<Sms> matching = new ArrayList<Sms>();
        for (int i = 0, smsCount = smses.size(); i < smsCount; i++) {
            Sms sms = smses.get(i);
            Matcher matcher = pattern.matcher(sms.body);
            if (matcher.matches()) {
                sms.code = matcher.group(1);
                matching.add(sms);
            }
        }

        Optional<Sms> matchingSms;
        if (matching.size() > 0) {
            matchingSms = Optional.of(matching.get(0));
        } else {
            matchingSms = Optional.absent();
        }


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset(Loader<Cursor>)");
    }


    private static class Sms {

        private final Integer id;
        private final Integer thread_id;
        private final String address;
        private final Object person;
        private final Integer date;
        private final Integer date_sent;
        private final Integer protocol;
        private final Integer read;
        private final Integer status;
        private final Integer type;
        private final Integer reply_path_present;
        private final Object subject;
        private final String body;
        private final String service_center;
        private final Integer locked;
        private final Integer error_code;
        private final Integer seen;

        private String code;

        Sms(Cursor data) {
            id = getIntOrNull(data, 0);
            thread_id = getIntOrNull(data, 1);
            address = getStringOrNull(data, 2);
            person = getIntOrNull(data, 3);
            date = getIntOrNull(data, 4);
            date_sent = getIntOrNull(data, 5);
            protocol = getIntOrNull(data, 6);
            read = getIntOrNull(data, 7);
            status = getIntOrNull(data, 8);
            type = getIntOrNull(data, 9);
            reply_path_present = getIntOrNull(data, 10);
            subject = getStringOrNull(data, 11);
            body = getStringOrNull(data, 12);
            service_center = getStringOrNull(data, 13);
            locked = getIntOrNull(data, 14);
            error_code = getIntOrNull(data, 15);
            seen = getIntOrNull(data, 16);
        }

        private Integer getIntOrNull(Cursor data, int columnIndex) {
            if (data.isNull(columnIndex)) return null;
            else return data.getInt(columnIndex);
        }

        private String getStringOrNull(Cursor data, int columnIndex) {
            if (data.isNull(columnIndex)) return null;
            else return data.getString(columnIndex);
        }

        @Override
        public String toString() {
            return "Sms{" +
                    "id=" + id +
                    ", thread_id=" + thread_id +
                    ", address='" + address + '\'' +
                    ", person=" + person +
                    ", date=" + date +
                    ", date_sent=" + date_sent +
                    ", protocol=" + protocol +
                    ", read=" + read +
                    ", status=" + status +
                    ", type=" + type +
                    ", reply_path_present=" + reply_path_present +
                    ", subject=" + subject +
                    ", body='" + body + '\'' +
                    ", service_center='" + service_center + '\'' +
                    ", locked=" + locked +
                    ", error_code=" + error_code +
                    ", seen=" + seen +
                    '}';
        }
    }
}
