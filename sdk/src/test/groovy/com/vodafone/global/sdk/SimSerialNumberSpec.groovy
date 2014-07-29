package com.vodafone.global.sdk
import android.content.Context
import android.telephony.TelephonyManager
import pl.polidea.robospock.RoboSpecification
import timber.log.Timber

import static android.Manifest.permission.READ_PHONE_STATE
import static android.content.pm.PackageManager.PERMISSION_DENIED
import static android.content.pm.PackageManager.PERMISSION_GRANTED
import static android.telephony.TelephonyManager.SIM_STATE_READY

public class SimSerialNumberSpec extends RoboSpecification {

    public static final String SIM_NUMBER = "1234"
    Context context
    TelephonyManager telephonyManager
    Timber.Tree tree

    def setup() {
        context = Mock(Context)
        tree = Mock(Timber.Tree)
        Timber.plant(tree)
        telephonyManager = Mock(TelephonyManager)
        context.getSystemService(Context.TELEPHONY_SERVICE) >> telephonyManager
    }

    def "SIM number is not read when permission is missing"() {
        context.checkCallingOrSelfPermission(READ_PHONE_STATE) >> PERMISSION_DENIED

        when:
        SimSerialNumber number = new SimSerialNumber(context)

        then:
        !number.isPresent()
        1 * tree.w("Application using Vodafone SDK doesn't have READ_PHONE_STATE permission")
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
