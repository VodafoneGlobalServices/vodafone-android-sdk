package com.vodafone.global.sdk;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import com.google.common.base.Optional;
import com.vodafone.global.sdk.logging.Logger;
import com.vodafone.global.sdk.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsInboxObserver {
    public static final Uri SMS_URI = Uri.parse("content://sms");
    static Logger log = LoggerFactory.getLogger();

    private final Context context;
    private final OnSmsInterceptionTimeoutCallback callback;
    final Pattern pattern;
    private final int smsValidationTimeoutInSeconds;
    private Timer timer;
    private SmsReceiver smsReceiver;

    private SmsContentObserver smsContentObserver;
    private long startTimestamp;

    private Lock lock = new ReentrantLock();
    private boolean intercepting = false;

    public SmsInboxObserver(Context context, Settings settings, OnSmsInterceptionTimeoutCallback callback) {
        this.context = context;
        this.callback = callback;
        String pinRegex = settings.smsInterceptionRegex();
        pattern = Pattern.compile(pinRegex);
        smsValidationTimeoutInSeconds = settings.smsValidationTimeoutInSeconds();
    }

    public void start() {
        lock.lock();

        try {
            if (intercepting) return;

            log.v("SmsInboxObserver :: starting interception");

            startTimestamp = System.currentTimeMillis();

            registerBroadcastReceiver();
            registerContentObserver();

            timer = new Timer("SmsInboxObserver");
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    stop();
                    log.v("SmsInboxObserver :: stopped interception due to timeout");
                    callback.onTimeout();
                }
            }, smsValidationTimeoutInSeconds * 1000);

            intercepting = true;
        } finally {
            lock.unlock();
        }
    }

    private void registerBroadcastReceiver() {
        log.v("SmsInboxObserver :: registerBroadcastReceiver");
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY - 1);
        smsReceiver = new SmsReceiver();
        context.registerReceiver(smsReceiver, filter);
    }

    private void registerContentObserver() {
        log.v("SmsInboxObserver :: registerContentObserver");
        smsContentObserver = new SmsContentObserver(new Handler(Looper.getMainLooper()));
        context.getContentResolver().registerContentObserver(SMS_URI, true, smsContentObserver);
    }

    public void stop() {
        lock.lock();

        try {
            if (intercepting) {
                timer.cancel();
                unregisterBroadcastReceiver();
                unregisterContentObserver();
                log.v("SmsInboxObserver :: stopped interception");

                intercepting = false;
            }
        } finally {
            lock.unlock();
        }
    }

    private void unregisterBroadcastReceiver() {
        log.v("SmsInboxObserver :: unregisterBroadcastReceiver");
        context.unregisterReceiver(smsReceiver);
        smsReceiver = null;
    }

    private void unregisterContentObserver() {
        log.v("SmsInboxObserver :: unregisterContentObserver");
        context.getContentResolver().unregisterContentObserver(smsContentObserver);
        smsContentObserver = null;
    }

    /**
     * SmsReceiver is responsible for intercepting sms messages and automatic pin validation.
     *
     * To enable SmsReceiver, 3rd party app needs to register receiver in AndroidManifest.xml with
     * <pre>{@code
     * <receiver android:name="com.vodafone.global.sdk.SmsInboxObserver.SmsReceiver">
     *   <intent-filter>
     *     <action android:name="android.provider.Telephony.SMS_RECEIVED" />
     *   </intent-filter>
     * </receiver>}</pre>
     * and obtain android.permission.RECEIVE_SMS by adding
     * <pre>{@code <uses-permission android:name="android.permission.RECEIVE_SMS" />}</pre>
     */
    private class SmsReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
                log.v("SmsReceiver :: received sms :: thread: " + Thread.currentThread().getName());
                List<String> smses = extractSmsesFromIntent(intent);
                List<String> pins = extractCodes(smses);
                notifySdk(pins);
            } else {
                log.w("SmsReceiver :: wrong action: " + intent.getAction());
            }
        }

        private List<String> extractSmsesFromIntent(Intent intent) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                return extractSmsBody19(intent);
            else
                return extractSmsBody(intent);
        }

        @TargetApi(19)
        private List<String> extractSmsBody19(Intent intent) {
            List<String> smses = new ArrayList<String>();
            for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent))
                smses.add(smsMessage.getMessageBody());
            return smses;
        }

        private List<String> extractSmsBody(Intent intent) {
            List<String> smses = new ArrayList<String>();
            Bundle bundle = intent.getExtras();
            if (bundle == null)
                return smses;
            for (Object pdu : (Object[]) bundle.get("pdus"))
                smses.add(SmsMessage.createFromPdu((byte[]) pdu).getMessageBody());
            return smses;
        }

        private List<String> extractCodes(List<String> smses) {
            List<String> codes = new ArrayList<String>();
            for (String sms : smses) {
                Matcher matcher = pattern.matcher(sms);
                if (matcher.matches()) {
                    log.d("SmsReceiver :: " + sms + ": matches regex");
                    String code = matcher.group(1);
                    codes.add(code);
                } else {
                    log.d("SmsReceiver :: " + sms + ": doesn't matches regex");
                }
            }
            return codes;
        }

        private void notifySdk(List<String> pins) {
            if (pins.size() > 0) {
                stop();
                String pin = pins.get(pins.size() - 1);
                callback.validateSmsCode(pin);
            }
        }
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
//            log.d("ContentObserver :: onChange" +
//                    "\n:: thread: " + Thread.currentThread().getName() +
//                    "\n:: uri: " + uri.toString());
            checkForMatchingSmsMessages();
        }

        void checkForMatchingSmsMessages() {
            Cursor data = context.getContentResolver().query(SMS_URI, null, null, null, "date ASC");

            ArrayList<Sms> smses = new ArrayList<Sms>();
            while (data.moveToNext()) {
                Sms sms = new Sms(data);
                if (sms.date.isPresent()
                        && sms.date.get() > startTimestamp
                        && sms.body.isPresent()
                ) {
                    smses.add(sms);
                }
            }

            ArrayList<Sms> matchingSmsMessages = new ArrayList<Sms>();
            for (int i = 0, smsCount = smses.size(); i < smsCount; i++) {
                Sms sms = smses.get(i);
                Matcher matcher = pattern.matcher(sms.body.get());
                if (matcher.matches()) {
                    sms.code = matcher.group(1);
                    matchingSmsMessages.add(sms);
                }
            }

            if (matchingSmsMessages.size() > 0) {
                stop();
                Sms newestSms = matchingSmsMessages.get(matchingSmsMessages.size() - 1);
                callback.validateSmsCode(newestSms.code);
            }
        }
    }

    private static class Sms {

//        private final Integer id;
//        private final Integer thread_id;
//        private final String address;
//        private final Object person;
        private final Optional<Long> date;
//        private final Long date_sent;
//        private final Integer protocol;
//        private final Integer read;
//        private final Integer status;
//        private final Integer type;
//        private final Integer reply_path_present;
//        private final Object subject;
        private final Optional<String> body;
//        private final String service_center;
//        private final Integer locked;
//        private final Integer error_code;
//        private final Integer seen;

        private String code;

        Sms(Cursor data) {
//            id = getIntOrNull(data, 0);
//            thread_id = getIntOrNull(data, 1);
//            address = getStringOrNull(data, 2);
//            person = getIntOrNull(data, 3);
            date = getLong(data, "date");
//            date_sent = getLongOrNull(data, 5);
//            protocol = getIntOrNull(data, 6);
//            read = getIntOrNull(data, 7);
//            status = getIntOrNull(data, 8);
//            type = getIntOrNull(data, 9);
//            reply_path_present = getIntOrNull(data, 10);
//            subject = getStringOrNull(data, 11);
            body = getString(data, "body");
//            service_center = getStringOrNull(data, 13);
//            locked = getIntOrNull(data, 14);
//            error_code = getIntOrNull(data, 15);
//            seen = getIntOrNull(data, 16);
        }

//        private Integer getIntOrNull(Cursor data, int columnIndex) {
//            if (data.isNull(columnIndex)) return null;
//            else return data.getInt(columnIndex);
//        }

        private Optional<Long> getLong(Cursor data, String columnName) {
            Optional<Long> result;
            int columnIndex = data.getColumnIndex(columnName);
            if (columnIndex == -1 || data.isNull(columnIndex))
                result = Optional.absent();
            else
                result = Optional.of(data.getLong(columnIndex));
            return result;
        }

        private Optional<String> getString(Cursor data, String columnName) {
            Optional<String> result;
            int columnIndex = data.getColumnIndex(columnName);
            if (columnIndex == -1 || data.isNull(columnIndex))
                result = Optional.absent();
            else
                result = Optional.of(data.getString(columnIndex));
            return result;
        }

        @Override
        public String toString() {
            return "Sms{" +
                    "date=" + date +
                    ", body='" + body + '\'' +
                    '}';
        }
    }

    public interface OnSmsInterceptionTimeoutCallback {
        void onTimeout();

        void validateSmsCode(String pin);
    }
}
