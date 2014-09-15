package com.vodafone.global.sdk;

import android.content.ContentResolver;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.telephony.TelephonyManager;

public class Utils {

    /**
     * Retrieves ANDROID_ID from system.
     * @see android.provider.Settings.Secure#ANDROID_ID
     */
    public static String getAndroidId(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        return Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID);
    }

    public static boolean isDataOverMobileNetwork(Context context) {
        ConnectivityManager mConnectivity = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = mConnectivity.getActiveNetworkInfo();
        if (info != null && info.getType() == ConnectivityManager.TYPE_MOBILE) {
            return info.isConnected();
        }
        return false;
    }

    public static boolean isDataOverWiFi(Context context) {
        ConnectivityManager mConnectivity = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = mConnectivity.getActiveNetworkInfo();
        if (info != null && info.getType() == ConnectivityManager.TYPE_WIFI) {
            return info.isConnected();
        }
        return false;
    }

    public static String getMCC(Context context) {
            String MCC = "";
            TelephonyManager tel = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
            String networkOperator = tel.getSimOperator();

            if (networkOperator != null && !networkOperator.isEmpty()) {
                MCC = networkOperator.substring(0, 3);
            }
            return MCC;
    }

    public static boolean isHasTimedOut(long timeMillis) {
        return System.currentTimeMillis() > timeMillis ? true : false;
    }
}
