package com.vodafone.global.sdk.http.oauth
import pl.polidea.robospock.RoboSpecification

class OAuthTokenSpec extends RoboSpecification {
    public static final String accessToken = "Kw4N7mrDg6Xdmu4GsNzVEzGshRNO"
    public static final String tokenType = "Bearer"
    public static final String expiresIn = "3599"
    public static final long expirationTime = 1234

    def "can create token"() {
        when:
        def token = new OAuthToken(accessToken, tokenType, expiresIn, expirationTime)

        then:
        token.accessToken == accessToken
        token.tokenType == tokenType
        token.expiresIn == expiresIn
        token.expirationTime == expirationTime
    }

    def "can't change token value (accessToken) after it was created"() {
        def token = new OAuthToken(accessToken, tokenType, expiresIn, expirationTime)

        when:
        token.accessToken = ""

        then:
        thrown(ReadOnlyPropertyException)
    }

    def "can't change token value (tokenType) after it was created"() {
        def token = new OAuthToken(accessToken, tokenType, expiresIn, expirationTime)

        when:
        token.tokenType = ""

        then:
        thrown(ReadOnlyPropertyException)
    }

    def "can't change token value (expiresIn) after it was created"() {
        def token = new OAuthToken(accessToken, tokenType, expiresIn, expirationTime)

        when:
        token.expiresIn = ""

        then:
        thrown(ReadOnlyPropertyException)
    }
}
