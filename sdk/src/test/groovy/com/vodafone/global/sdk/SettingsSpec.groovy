package com.vodafone.global.sdk
import android.content.Context
import android.content.res.Resources
import pl.polidea.robospock.RoboSpecification

class SettingsSpec extends RoboSpecification {
    Context context

    def setup() {
        Resources res = Mock(Resources)
        def stream = SettingsSpec.class.getResourceAsStream("/config.json")
        res.openRawResource(R.raw.config) >> stream
        context = Mock(Context)
        context.resources >> res
    }

    def "settings get initialized properly from file"() {
        when:
        new Settings(context)

        then:
        noExceptionThrown()
    }
}
