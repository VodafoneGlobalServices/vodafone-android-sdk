package com.vodafone.global.sdk;

import java.util.LinkedList;

public class MaximumThresholdChecker {
    private final long maxNumberOfCalls;
    private final long timeIntervalInMs;
    private final Clock clock;
    private final LinkedList<Long> callTimestamps = new LinkedList<Long>();

    /**
     * @param maxNumberOfCalls maximum number of calls
     * @param timeIntervalInSeconds time interval in ms during which the number of maximum call can be made
     */
    public MaximumThresholdChecker(
            long maxNumberOfCalls,
            long timeIntervalInSeconds
    ) {
        this.maxNumberOfCalls = maxNumberOfCalls;
        this.timeIntervalInMs = timeIntervalInSeconds * 1000;
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
        this.timeIntervalInMs = timeInterval * 1000;
        this.clock = clock;
    }

    /**
     * Checks if maximum number of requests within a specified period of time was exceeded.
     */
    public boolean thresholdReached() {
        Long currentTime = clock.currentTimeMillis();
        callTimestamps.add(currentTime);
        filterOutTimestampsBelowTimeThreshold(currentTime);
        int numberOfCallsMadeInInterval = numberOfCallsMadeInInterval();
        if (maxNumberOfCalls > 0) {
            return numberOfCallsMadeInInterval > maxNumberOfCalls;
        } else {
            return true;
        }
    }

    private void filterOutTimestampsBelowTimeThreshold(Long currentTime) {
        long timeThreshold = currentTime - timeIntervalInMs;
        boolean anyTimestampsLeft = anyTimestampsLeft();
        Long oldestTimestamp = oldestTimestamp();
        while (anyTimestampsLeft && oldestTimestamp <= timeThreshold) {
            removeOldestTimestamp();

            anyTimestampsLeft = anyTimestampsLeft();
            oldestTimestamp = oldestTimestamp();
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
