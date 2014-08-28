package com.vodafone.global.sdk.http.resolve

import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.mockwebserver.MockResponse
import com.squareup.okhttp.mockwebserver.MockWebServer
import com.squareup.okhttp.mockwebserver.RecordedRequest
import com.vodafone.global.sdk.SimSerialNumber
import com.vodafone.global.sdk.UserDetails
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat
import spock.lang.Specification

class ResolvePostRequestSpec extends Specification {
    def resolved = true;
    def token = "a183f2fc-c98d-4863-962d-4d739c78abc4"
    def path = "/he/users/token/" + token
    def etag = "ar62d6f2d65af65df"
    def userDetails = userDetails()
    def userDetailsDTO = new UserDetailsDTO(userDetails, etag)
    def String accessToken = "1"
    def String androidId = "2"
    def String mobileCountryCode = "DE"
    def String sdkId = "3"
    def String appId = "4"
    def boolean stillRunning = true
    def String source = "?"
    def boolean tetheringConflict = false
    def boolean secure = false
    def Date expires = new Date()
    def boolean validationRequired = false
    SimSerialNumber imsi

    MockWebServer server
    private String imsiValue = ""

    def setup() {
        server = new MockWebServer()

        imsi = Mock()
        imsi.isPresent() >> true
        imsi.get() >> imsiValue
    }

    def cleanup() {
        server.shutdown()
    }

    def "request returns correct object when 200 OK is received"() {
        def date = "2014-08-28T12:00:00+01:00"
        DateTimeFormatter parser = ISODateTimeFormat.dateTimeNoMillis();
        Date expires = parser.parseDateTime(date).toDate();
        def body =
        """
            {
              "resolved": ${resolved},
              "stillRunning": ${stillRunning},
              "source": ${source},
              "token": "${token}",
              "tetheringConflict": ${tetheringConflict},
              "secure": ${secure},
              "expires": "${date}",
              "validationRequired": ${validationRequired}
            }
        """
        def newEtag = "lkajsdf8eyafhuewh"
        def response = new MockResponse().setBody(body).setHeader("etag", newEtag)
        server.enqueue(response)
        server.play()
        def url = server.getUrl(path).toString()
        ResolvePostRequest request = prepareRequest(url, imsi)

        when:
        def userDetailsDTO = request.loadDataFromNetwork()

        then:
        def userDetails = userDetailsDTO.userDetails
        userDetails.resolved == resolved
        userDetails.stillRunning == stillRunning
        userDetails.token == token
        userDetails.expires == expires
        userDetails.validationRequired == validationRequired
        userDetailsDTO.etag == newEtag

        RecordedRequest request1 = server.takeRequest()
        request1.path == path
        request1.method == "POST"
    }

    def "exception is thrown when HTTP code not supported"() {
        def response = new MockResponse().setResponseCode(400)
        server.enqueue(response)
        server.play()
        def url = server.getUrl(path).toString()
        ResolvePostRequest request = prepareRequest(url, imsi)

        when:
        request.loadDataFromNetwork()

        then:
        thrown(IllegalStateException)

        RecordedRequest request1 = server.takeRequest()
        request1.path == path
        request1.method == "POST"
    }

    private ResolvePostRequest prepareRequest(String url, SimSerialNumber imsi) {
        def request = ResolvePostRequest.builder()
                .url(url)
                .accessToken(accessToken)
                .androidId(androidId)
                .mobileCountryCode(mobileCountryCode)
                .sdkId(sdkId)
                .appId(appId)
                .imsi(imsi)
                .build()
        request.okHttpClient = new OkHttpClient()
        return request
    }

    private UserDetails userDetails() {
        UserDetails.builder()
                .resolved(resolved)
                .stillRunning(stillRunning)
                .token(token)
                .expires(expires)
                .validationRequired(validationRequired)
                .build()
    }
}
