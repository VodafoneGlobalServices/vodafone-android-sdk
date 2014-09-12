package com.vodafone.global.sdk;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by bamik on 2014-09-11.
 */
public class MaximumTresholdChecker {
    private long retryCallLimit;
    private long retryIntervalLimitMs;
    private Queue<Long> requestStack = new LinkedList<Long>();

    public MaximumTresholdChecker(long retryCallLimit, long retryIntervalLimitMs) {
        this.retryCallLimit = retryCallLimit;
        this.retryIntervalLimitMs = retryIntervalLimitMs;
    }

    public void setRetryCallLimit(long retryCallLimit) {
        this.retryCallLimit = retryCallLimit;
    }

    public void retryIntervalLimitMs(long retryIntervalLimitMs) {
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
