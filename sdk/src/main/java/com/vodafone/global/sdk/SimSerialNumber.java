package com.vodafone.global.sdk;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;
import com.google.common.base.Optional;
import timber.log.Timber;

/**
 * Class responsible for retrieving SIM serial number.
 */
public class SimSerialNumber {
    private final Optional<String> simSerialNumber;

    public SimSerialNumber(Context context) {
        int result = context.checkCallingOrSelfPermission(Manifest.permission.READ_PHONE_STATE);

        switch (result) {
            case PackageManager.PERMISSION_GRANTED:
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

                if (telephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY) {
                    simSerialNumber = Optional.fromNullable(telephonyManager.getSimSerialNumber());
                } else {
                    simSerialNumber = Optional.absent();
                }

                break;
            case PackageManager.PERMISSION_DENIED:
                simSerialNumber = Optional.absent();
                Timber.w("Application using Vodafone SDK doesn't have READ_PHONE_STATE permission");
                break;
            default:
                // can happen only if behavior of Android SDK changed
                simSerialNumber = Optional.absent();
                Timber.e("Unknown type from Context.checkCallingOrSelfPermission");
                break;
        }
    }

    /**
     * Returns {@code true} if SIM serial number is available.
     */
    public boolean isPresent() {
        return simSerialNumber.isPresent();
    }

    /**
     * Returns the SIM serial number, which must be present. If the SIM serial number might be
     * absent, use {@link #or(String)} or {@link #orNull} instead.
     *
     * @throws IllegalStateException if the SIM serial number is unavailable ({@link #isPresent} returns {@code false})
     */
    public String get() {
        try {
            return simSerialNumber.get();
        } catch (Exception e) {
            throw new IllegalStateException("SimSerialNumber.get() cannot be called on an absent value");
        }
    }

    /**
     * Returns the SIM serial number if it is present; {@code defaultValue} otherwise. If
     * no default value should be required because the instance is known to be present, use
     * {@link #get()} instead. For a default value of {@code null}, use {@link #orNull}.
     */
    public String or(String defaultValue) {
        return simSerialNumber.or(defaultValue);
    }

    /**
     * Returns the SIM serial number if it is present; {@code null} otherwise. If the
     * instance is known to be present, use {@link #get()} instead.
     */
    public String orNull() {
        return simSerialNumber.orNull();
    }
}
