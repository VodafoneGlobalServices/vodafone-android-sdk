package com.vodafone.global.sdk.http.oauth
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.mockwebserver.MockResponse
import com.squareup.okhttp.mockwebserver.MockWebServer
import com.squareup.okhttp.mockwebserver.RecordedRequest
import spock.lang.Ignore
import spock.lang.Specification

@Ignore
class OAuthTokenRequestSpec extends Specification {

    def POST = "POST"
    def anyClient = "123"
    def secret = "secret"
    def path = "/2/oauth/access-token"
    def accessToken = "A3lkjdsf65sfFJJH734jhuvEdTE7"
    def tokenType = "Bearer"
    def expiresIn = "3599"
    def scope = "scope"
    def grantType = "grantType"

    MockWebServer server

    def setup() {
        server = new MockWebServer()
    }

    def cleanup() {
        server.shutdown()
    }

    def "request for oauth2 token returns correct token dto"() {
        def body =
        """
            {
              "access_token": "${accessToken}",
              "token_type": "${tokenType}",
              "expires_in": "${expiresIn}"
            }
        """
        def response = new MockResponse().setBody(body)
        server.enqueue(response)
        server.play()

        def url = server.getUrl(path).toString()

        OAuthTokenRequest request = prepareRequest(url)

        when:
        def token = request.loadDataFromNetwork()

        then:
        token.accessToken == accessToken
        token.tokenType == tokenType
        token.expiresIn == expiresIn

        RecordedRequest request1 = server.takeRequest()
        request1.path == path
        request1.method == POST
    }

    def "exception is thrown when HTTP code != 200"() {
        server.enqueue(new MockResponse().setResponseCode(400))
        server.play()

        def url = server.getUrl(path).toString()
        OAuthTokenRequest request = prepareRequest(url)

        when:
        request.loadDataFromNetwork()

        then:
        thrown(AuthorizationFailed)

        RecordedRequest request1 = server.takeRequest()
        request1.path == path
        request1.method == POST
    }

    private OAuthTokenRequest prepareRequest(String url) {
        def request = OAuthTokenRequest.builder()
                .url(url)
                .clientId(anyClient)
                .clientSecret(secret)
                .scope(scope)
                .grantType(grantType)
                .build()
        request.okHttpClient = new OkHttpClient()
        return request
    }
}
