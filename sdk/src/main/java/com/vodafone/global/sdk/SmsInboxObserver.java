package com.vodafone.global.sdk;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import com.vodafone.global.sdk.logging.Logger;
import com.vodafone.global.sdk.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsInboxObserver {
    private static final Uri SMS_URI = Uri.parse("content://sms/");
    private static Logger log = LoggerFactory.getLogger();

    private final Context context;
    private final OnSmsInterceptionTimeoutCallback callback;
    private final Pattern pattern;
    private SmsContentObserver smsContentObserver;
    private final int smsValidationTimeoutInSeconds;
    private Timer timer;

    public SmsInboxObserver(Context context, Settings settings, OnSmsInterceptionTimeoutCallback callback) {
        this.context = context;
        this.callback = callback;
        String pinRegex = settings.smsInterceptionRegex();
        pattern = Pattern.compile(pinRegex);
        smsValidationTimeoutInSeconds = settings.smsValidationTimeoutInSeconds();
    }

    public void start() {
        log.d("SmsInboxObserver::starting interception");
        smsContentObserver = new SmsContentObserver(new Handler(Looper.getMainLooper()));
        context.getContentResolver()
                .registerContentObserver(SMS_URI, true, smsContentObserver);
        timer = new Timer("SmsInboxObserver");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                log.d("SmsInboxObserver::stoped interception due to timeout");
                callback.onTimeout();
                stop();
            }
        }, smsValidationTimeoutInSeconds * 1000);
    }

    public void stop() {
        if (smsContentObserver != null) {
            timer.cancel();
            context.getContentResolver().unregisterContentObserver(smsContentObserver);
            log.d("SmsInboxObserver::stoped interception");
        }
        smsContentObserver = null;
    }

    private class SmsContentObserver extends ContentObserver {
        public SmsContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            Cursor data = context.getContentResolver().query(SMS_URI, null, null, null, "date ASC");

            ArrayList<Sms> smses = new ArrayList<Sms>();
            while (data.moveToNext()) {
                Sms sms = new Sms(data);
                smses.add(sms);
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

            log.d("SmsInboxObserver::found " + matching.size() + " smses matching regex");

            if (matching.size() > 0) {
                Vodafone.validateSmsCode(matching.get(0).code);

                for (int i = 0, matchingCount = matching.size(); i < matchingCount; i++) {
                    Sms sms = matching.get(i);
                    context.getContentResolver().delete(SMS_URI, "_id=?", new String[]{String.valueOf(sms.id)});
                    log.d("SmsInboxObserver::removed sms with id: " + sms.id + ", body: " + sms.body);
                }

                stop();
            }
        }
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

    public interface OnSmsInterceptionTimeoutCallback {
        void onTimeout();
    }
}
