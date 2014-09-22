package com.vodafone.global.sdk;

import java.util.LinkedList;
import java.util.Queue;

public class MaximumTresholdChecker {
    private final long retryCallLimit;
    private final long retryIntervalLimitMs;
    private Queue<Long> requestStack = new LinkedList<Long>();

    public MaximumTresholdChecker(long retryCallLimit, long retryIntervalLimitMs) {
        this.retryCallLimit = retryCallLimit;
        this.retryIntervalLimitMs = retryIntervalLimitMs;
    }

    public boolean isMaximumThresholdReached() {
        boolean maximumThresholdReach = true;
        Long currentTime = System.currentTimeMillis();

        requestStack.add(currentTime);
        while (requestStack.peek() < (currentTime - retryIntervalLimitMs)) {
            requestStack.remove();
        }

        if (retryCallLimit < requestStack.size()) {
            maximumThresholdReach = false;
        }
        return maximumThresholdReach;
    }
}
