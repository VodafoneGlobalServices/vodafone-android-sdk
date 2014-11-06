package com.vodafone.global.sdk;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import com.vodafone.global.sdk.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            LoggerFactory.getLogger().d(TAG, "received sms");
            List<String> smses = extractSmsesFromIntent(intent);
            List<String> pins = extractCodes(smses);
            notifySdk(pins);
        } else {
            LoggerFactory.getLogger().w(TAG, "wrong action: " + intent.getAction());
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
        Pattern pattern = Pattern.compile("^.+: (\\d{4})$");
        for (String sms : smses) {
            Matcher matcher = pattern.matcher(sms);
            if (matcher.matches()) {
                LoggerFactory.getLogger().d(TAG, sms + ": matches regex");
                String code = matcher.group(1);
                codes.add(code);
            } else {
                LoggerFactory.getLogger().d(TAG, sms + ": doesn't matches regex");
            }
        }
        return codes;
    }

    private void notifySdk(List<String> pins) {
        for (String pin : pins) {
            LoggerFactory.getLogger().d(TAG, "Vodafone.validateSmsCode(" + pin + ")");
            Vodafone.validateSmsCode(pin);
        }
    }
}
