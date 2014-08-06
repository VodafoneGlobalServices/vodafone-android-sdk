package com.vodafone.global.sdk
import android.content.Context
import android.content.res.AssetManager
import pl.polidea.robospock.RoboSpecification

class SettingsSpec extends RoboSpecification {
    Context context

    def setup() {
        AssetManager manager = Mock(AssetManager)
        def stream = SettingsSpec.class.getResourceAsStream("/config.json")
        manager.open("config.json") >> stream
        context = Mock(Context)
        context.assets >> manager
    }

    def "settings get initialized properly from file"() {
        when:
        new Settings(context)

        then:
        noExceptionThrown()
    }
}
