package com.vodafone.global.sdk;

import java.util.LinkedList;

public class MaximumThresholdChecker {
    private final long maxNumberOfCalls;
    private final long timeInterval;
    private final Clock clock;
    private final LinkedList<Long> callTimestamps = new LinkedList<Long>();

    /**
     * @param maxNumberOfCalls maximum number of calls
     * @param timeInterval time interval in ms during which the number of maximum call can be made
     */
    public MaximumThresholdChecker(
            long maxNumberOfCalls,
            long timeInterval
    ) {
        this.maxNumberOfCalls = maxNumberOfCalls;
        this.timeInterval = timeInterval;
        clock = new Clock() {
            @Override
            public Long currentTimeMillis() {
                return System.currentTimeMillis();
            }
        };
    }

    /**
     * Special constructor that should be used only for testing.
     * @param maxNumberOfCalls maximum number of calls
     * @param timeInterval time interval during which the number of maximum call can be made
     * @param clock special implementation of Clock for testing purposes
     */
    MaximumThresholdChecker(
            long maxNumberOfCalls,
            long timeInterval,
            Clock clock
    ) {
        this.maxNumberOfCalls = maxNumberOfCalls;
        this.timeInterval = timeInterval;
        this.clock = clock;
    }

    /**
     * Checks if maximum number of requests within a specified period of time was exceeded.
     */
    public boolean thresholdReached() {
        Long currentTime = clock.currentTimeMillis();
        callTimestamps.add(currentTime);
        filterOutTimestampsBelowTimeThreshold(currentTime);
        return numberOfCallsMadeInInterval() > maxNumberOfCalls;
    }

    private void filterOutTimestampsBelowTimeThreshold(Long currentTime) {
        long timeThreshold = currentTime - timeInterval;
        while (anyTimestampsLeft() && oldestTimestamp() <= timeThreshold) {
            removeOldestTimestamp();
        }
    }

    private boolean anyTimestampsLeft() {
        return !callTimestamps.isEmpty();
    }

    private Long oldestTimestamp() {
        return callTimestamps.peek();
    }

    private Long removeOldestTimestamp() {
        return callTimestamps.remove();
    }

    private int numberOfCallsMadeInInterval() {
        return callTimestamps.size();
    }
}
