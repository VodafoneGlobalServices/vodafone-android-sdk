package com.vodafone.global.sdk

import spock.lang.Specification

class MaximumThresholdCheckerSpec extends Specification {
    def maxNumberOfCalls = 3
    def timeInterval = 10;
    Clock clock
    MaximumThresholdChecker checker

    def setup() {
        clock = Mock(Clock)
        checker = new MaximumThresholdChecker(maxNumberOfCalls, timeInterval, clock);
    }

    def "threshold is not reached after creation"() {
        1 * clock.currentTimeMillis() >> 1

        expect:
        !checker.thresholdReached()
    }

    def "threshold is not reached when max number of calls have been made"() {
        3 * clock.currentTimeMillis() >>> [1000, 2000, 3000]

        when:
        def firstCheck = checker.thresholdReached()
        def secondCheck = checker.thresholdReached()
        def thirdCheck = checker.thresholdReached()

        then:
        firstCheck == false
        secondCheck == false
        thirdCheck == false
    }

    def "threshold is reached with more than max number of calls have been made"() {
        4 * clock.currentTimeMillis() >>> [1000, 2000, 3000, 4000]

        when:
        def firstCheck = checker.thresholdReached()
        def secondCheck = checker.thresholdReached()
        def thirdCheck = checker.thresholdReached()
        def forthCheck = checker.thresholdReached()

        then:
        firstCheck == false
        secondCheck == false
        thirdCheck == false
        forthCheck == true
    }

    def "threshold is not reached when last calls is made at the end of interval"() {
        4 * clock.currentTimeMillis() >>> [1000, 2000, timeInterval * 1000, timeInterval * 1000 + 1]

        when:
        def firstCheck = checker.thresholdReached()
        def secondCheck = checker.thresholdReached()
        def thirdCheck = checker.thresholdReached()
        def forthCheck = checker.thresholdReached()

        then:
        firstCheck == false
        secondCheck == false
        thirdCheck == false
        forthCheck == true
    }
}
