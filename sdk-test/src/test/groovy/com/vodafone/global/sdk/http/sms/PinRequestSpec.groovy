package com.vodafone.global.sdk.http.sms

import com.squareup.okhttp.mockwebserver.MockWebServer
import spock.lang.Specification

class PinRequestSpec extends Specification {
    MockWebServer server

    def setup() {
        server = new MockWebServer()
    }

    def cleanup() {
        server.shutdown()
    }

    def "empty"() {
        
    }
}
