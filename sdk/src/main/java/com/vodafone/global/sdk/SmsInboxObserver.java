package com.vodafone.global.sdk;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import com.vodafone.global.sdk.logging.Logger;
import com.vodafone.global.sdk.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsInboxObserver {
    private static Logger log = LoggerFactory.getLogger();

    private final Context context;
    private final OnSmsInterceptionTimeoutCallback callback;
    private final Pattern pattern;
    private final int smsValidationTimeoutInSeconds;
    private Timer timer;
    private SmsReceiver smsReceiver;

    public SmsInboxObserver(Context context, Settings settings, OnSmsInterceptionTimeoutCallback callback) {
        this.context = context;
        this.callback = callback;
        String pinRegex = settings.smsInterceptionRegex();
        pattern = Pattern.compile(pinRegex);
        smsValidationTimeoutInSeconds = settings.smsValidationTimeoutInSeconds();
    }

    public void start() {
        log.d("SmsInboxObserver :: starting interception");
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY - 1);
        smsReceiver = new SmsReceiver();
        context.registerReceiver(smsReceiver, filter);

        timer = new Timer("SmsInboxObserver");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                log.d("SmsInboxObserver :: stopped interception due to timeout");
                callback.onTimeout();
                stop();
            }
        }, smsValidationTimeoutInSeconds * 1000);
    }

    public void stop() {
        if (smsReceiver != null) {
            timer.cancel();
            context.unregisterReceiver(smsReceiver);
            log.d("SmsInboxObserver :: stopped interception");
        }
        smsReceiver = null;
    }

    /**
     * SmsReceiver is responsible for intercepting sms messages and automatic pin validation.
     *
     * To enable SmsReceiver, 3rd party app needs to register receiver in AndroidManifest.xml with
     * <pre>{@code
     * <receiver android:name="com.vodafone.global.sdk.SmsReceiver">
     *   <intent-filter>
     *     <action android:name="android.provider.Telephony.SMS_RECEIVED" />
     *   </intent-filter>
     * </receiver>}</pre>
     * and obtain android.permission.RECEIVE_SMS by adding
     * <pre>{@code <uses-permission android:name="android.permission.RECEIVE_SMS" />}</pre>
     */
    public class SmsReceiver extends BroadcastReceiver {

        public static final String TAG = "SmsReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
                log.d(TAG, "received sms");
                List<String> smses = extractSmsesFromIntent(intent);
                List<String> pins = extractCodes(smses);
                notifySdk(pins);
            } else {
                log.w("wrong action: " + intent.getAction());
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
                    log.d(TAG, sms + ": matches regex");
                    String code = matcher.group(1);
                    codes.add(code);
                } else {
                    log.d(TAG, sms + ": doesn't matches regex");
                }
            }
            return codes;
        }

        private void notifySdk(List<String> pins) {
            if (pins.size() > 0) {
                String pin = pins.get(pins.size() - 1);
                Vodafone.validateSmsCode(pin);
            }
        }
    }

    public interface OnSmsInterceptionTimeoutCallback {
        void onTimeout();
    }
}
