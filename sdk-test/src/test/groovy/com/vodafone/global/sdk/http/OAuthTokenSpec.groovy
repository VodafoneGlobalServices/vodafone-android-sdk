package com.vodafone.global.sdk.http

import pl.polidea.robospock.RoboSpecification

class OAuthTokenSpec extends RoboSpecification {
    public static final String accessToken = "Kw4N7mrDg6Xdmu4GsNzVEzGshRNO"
    public static final String tokenType = "Bearer"
    public static final String expiresIn = "3599"

    def "can create token"() {
        when:
        def token = new OAuthToken(accessToken, tokenType, expiresIn)

        then:
        token.accessToken == accessToken
        token.tokenType == tokenType
        token.expiresIn == expiresIn
    }

    def "can't change token value (accessToken) after it was created"() {
        def token = new OAuthToken(accessToken, tokenType, expiresIn)

        when:
        token.accessToken = ""

        then:
        thrown(ReadOnlyPropertyException)
    }

    def "can't change token value (tokenType) after it was created"() {
        def token = new OAuthToken(accessToken, tokenType, expiresIn)

        when:
        token.tokenType = ""

        then:
        thrown(ReadOnlyPropertyException)
    }

    def "can't change token value (expiresIn) after it was created"() {
        def token = new OAuthToken(accessToken, tokenType, expiresIn)

        when:
        token.expiresIn = ""

        then:
        thrown(ReadOnlyPropertyException)
    }
}
