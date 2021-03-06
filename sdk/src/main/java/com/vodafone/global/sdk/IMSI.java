package com.vodafone.global.sdk;

import android.content.Context;
import android.telephony.TelephonyManager;

import java.util.List;

import static android.Manifest.permission.READ_PHONE_STATE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class IMSI {

    private final String imsi;
    private final boolean mccAndMncOfSimCardBelongToVodafone;

    /**
     * Requires Permission:
     *   {@link android.Manifest.permission#READ_PHONE_STATE READ_PHONE_STATE}
     */
    public IMSI(Context context, List<String> supportedMccAndMnc) {
        TelephonyManager systemService = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (context.checkCallingOrSelfPermission(READ_PHONE_STATE) == PERMISSION_GRANTED) {
            imsi = systemService.getSubscriberId();
        } else {
            imsi = null;
        }
        String simOperator = systemService.getSimOperator();
        mccAndMncOfSimCardBelongToVodafone = checkMccAndMnc(simOperator, supportedMccAndMnc);
    }

    /**
     * Checks if SIM/IMSI card is supported by SDK.
     * @return {@code true} if it's supported, {@code false} otherwise
     */
    private boolean checkMccAndMnc(String simOperator, List<String> supportedMccAndMnc) {
        if ((simOperator != null && !simOperator.isEmpty())) {
            for (String mccAndMnc : supportedMccAndMnc) {
                if (simOperator.startsWith(mccAndMnc)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isValid() {
        return isPresent() && mccAndMncOfSimCardBelongToVodafone();
    }

    public boolean isPresent() {
        return imsi != null;
    }

    public boolean mccAndMncOfSimCardBelongToVodafone() {
        return mccAndMncOfSimCardBelongToVodafone;
    }

    public String get() {
        if (!isPresent())
            throw new IllegalStateException("IMSI.get() cannot be called on an absent value");
        return imsi;
    }
}
