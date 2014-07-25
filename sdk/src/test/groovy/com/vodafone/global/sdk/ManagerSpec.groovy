package com.vodafone.global.sdk

import android.content.Context
import android.telephony.TelephonyManager
import spock.lang.Specification

import static android.Manifest.permission.READ_PHONE_STATE
import static android.content.pm.PackageManager.PERMISSION_GRANTED
import static android.telephony.TelephonyManager.SIM_STATE_READY

class SimSerialNumberSpec extends Specification {

    public static final String SIM_NUMBER = "1234"
    def Context context
    def TelephonyManager telephonyManager

    def setup() {
        context = Mock(Context)
        telephonyManager = Mock(TelephonyManager)
    }

    def "SIM number is read when permission is set and sim state is ready"() {
        telephonyManager.getSimState() >> SIM_STATE_READY
        telephonyManager.getSimSerialNumber() >> SIM_NUMBER
        context.checkCallingOrSelfPermission(READ_PHONE_STATE) >> PERMISSION_GRANTED
        context.getSystemService(Context.TELEPHONY_SERVICE) >> telephonyManager

        when:
        def number = new SimSerialNumber(context)

        then:
        number.isPresent()
        number.get() == SIM_NUMBER
    }
}
