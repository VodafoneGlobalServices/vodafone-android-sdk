package com.vodafone.global.sdk

import android.content.Context
import android.telephony.TelephonyManager
import pl.polidea.robospock.RoboSpecification
import spock.lang.Unroll
import timber.log.Timber

import static android.Manifest.permission.READ_PHONE_STATE
import static android.content.pm.PackageManager.PERMISSION_DENIED
import static android.content.pm.PackageManager.PERMISSION_GRANTED
import static android.telephony.TelephonyManager.*

public class SimSerialNumberSpec extends RoboSpecification {

    static final String DEFAULT_SIM_NUMBER = "123"
    static final String SIM_NUMBER = "1234"
    static final int UNKNOWN_PERMISSION_VALUE = -2
    Context context
    TelephonyManager telephonyManager
    Timber.Tree log

    def setup() {
        context = Mock(Context)
        log = Mock(Timber.Tree)
        Timber.plant(log)
        telephonyManager = Mock(TelephonyManager)
        context.getSystemService(Context.TELEPHONY_SERVICE) >> telephonyManager
    }

    def "SIM number is not read when permission is missing"() {
        context.checkCallingOrSelfPermission(READ_PHONE_STATE) >> PERMISSION_DENIED

        when:
        SimSerialNumber number = new SimSerialNumber(context)

        then:
        !number.isPresent()
        1 * log.w("Application using Vodafone SDK doesn't have READ_PHONE_STATE permission")
    }

    @Unroll
    def "SIM number is not read when SIM is not ready"() {
        telephonyManager.getSimState() >> sim_state

        when:
        SimSerialNumber number = new SimSerialNumber(context)

        then:
        !number.present
        1 * log.w("SIM card is not ready")

        where:
        sim_state << [SIM_STATE_UNKNOWN, SIM_STATE_ABSENT, SIM_STATE_PIN_REQUIRED, SIM_STATE_PUK_REQUIRED, SIM_STATE_NETWORK_LOCKED]
    }

    def "warning is logged when permission state can't be determined"() {
        context.checkCallingOrSelfPermission(READ_PHONE_STATE) >> UNKNOWN_PERMISSION_VALUE

        when:
        SimSerialNumber number = new SimSerialNumber(context)

        then:
        !number.present
        1 * log.e("Unknown type from Context.checkCallingOrSelfPermission")
    }

    def "SimSerialNumber.get() throws IllegalStateException when SIM number can't be read"() {
        context.checkCallingOrSelfPermission(READ_PHONE_STATE) >> PERMISSION_DENIED
        SimSerialNumber number = new SimSerialNumber(context)

        when:
        number.get()

        then:
        def ex = thrown(IllegalStateException)
        ex.message == "SimSerialNumber.get() cannot be called on an absent value"
    }

    def "SimSerialNumber.or() returns default value when SIM number is absent"() {
        context.checkCallingOrSelfPermission(READ_PHONE_STATE) >> PERMISSION_DENIED

        when:
        SimSerialNumber number = new SimSerialNumber(context)

        then:
        !number.present
        number.or(DEFAULT_SIM_NUMBER) == DEFAULT_SIM_NUMBER
    }

    def "SimSerialNumber.or() returns real value when SIM number is present"() {
        context.checkCallingOrSelfPermission(READ_PHONE_STATE) >> PERMISSION_GRANTED
        telephonyManager.getSimState() >> SIM_STATE_READY
        telephonyManager.getSimSerialNumber() >> SIM_NUMBER

        when:
        SimSerialNumber number = new SimSerialNumber(context)

        then:
        number.present
        number.or(DEFAULT_SIM_NUMBER) == SIM_NUMBER
    }

    def "SimSerialNumber.orNull() returns NULL when SIM number is absent"() {
        context.checkCallingOrSelfPermission(READ_PHONE_STATE) >> PERMISSION_DENIED

        when:
        SimSerialNumber number = new SimSerialNumber(context)

        then:
        !number.present
        number.orNull() == null
    }

    def "SimSerialNumber.orNull() returns real value when SIM number is present"() {
        context.checkCallingOrSelfPermission(READ_PHONE_STATE) >> PERMISSION_GRANTED
        telephonyManager.getSimState() >> SIM_STATE_READY
        telephonyManager.getSimSerialNumber() >> SIM_NUMBER

        when:
        SimSerialNumber number = new SimSerialNumber(context)

        then:
        number.present
        number.orNull() == SIM_NUMBER
    }

    def "SIM number is read when permission is set and sim state is ready"() {
        telephonyManager.getSimState() >> SIM_STATE_READY
        telephonyManager.getSimSerialNumber() >> SIM_NUMBER
        context.checkCallingOrSelfPermission(READ_PHONE_STATE) >> PERMISSION_GRANTED

        when:
        SimSerialNumber number = new SimSerialNumber(context)

        then:
        number.isPresent()
        number.get() == SIM_NUMBER
    }
}
