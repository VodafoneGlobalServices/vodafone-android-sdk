package com.vodafone.global.sdk;

import android.content.Context;
import android.telephony.TelephonyManager;

import java.util.List;

public class IMSI {

    private final String imsi;
    private final boolean mccAndMncOfSimCardBelongToVodafone;

    /**
     * Requires Permission:
     *   {@link android.Manifest.permission#READ_PHONE_STATE READ_PHONE_STATE}
     */
    public IMSI(Context context, List<String> supportedMccAndMnc) {
        TelephonyManager systemService = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        imsi = systemService.getSubscriberId();
        mccAndMncOfSimCardBelongToVodafone = checkMccAndMnc(imsi, supportedMccAndMnc);
    }

    /**
     * Checks if SIM/IMSI card is supported by SDK.
     * @return {@code true} if it's supported, {@code false} otherwise
     */
    private boolean checkMccAndMnc(String imsi, List<String> supportedMccAndMnc) {
        for (String mccAndMnc : supportedMccAndMnc)
            if (imsi.startsWith(mccAndMnc))
                return true;

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
        if (!isPresent()) throw new IllegalStateException("IMSI.get() cannot be called on an absent value");
        return imsi;
    }
}
