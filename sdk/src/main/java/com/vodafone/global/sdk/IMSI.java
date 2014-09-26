package com.vodafone.global.sdk;

import android.content.Context;
import android.telephony.TelephonyManager;

public class IMSI {

    private final String imsi;

    /**
     * Requires Permission:
     *   {@link android.Manifest.permission#READ_PHONE_STATE READ_PHONE_STATE}
     */
    public IMSI(Context context) {
        TelephonyManager systemService = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        imsi = systemService.getSubscriberId();
    }

    public boolean isValid() {
        return isPresent() && mccAndMncOfSimCardBelongToVodafone();
    }

    public boolean isPresent() {
        return imsi != null;
    }

    public boolean mccAndMncOfSimCardBelongToVodafone() {
        return true; // TODO validation with data from configuration server
    }
}
