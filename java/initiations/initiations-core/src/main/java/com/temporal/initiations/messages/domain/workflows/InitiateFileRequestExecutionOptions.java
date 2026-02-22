package com.temporal.initiations.messages.domain.workflows;

import java.time.Duration;
import java.util.Objects;

public final class InitiateFileRequestExecutionOptions {
    private boolean autoApprove;
    private int ttlSeconds;
    private int maxBatchCount;
    public boolean isAutoApprove() {
        return autoApprove;
    }

    public void setAutoApprove(boolean autoApprove) {
        this.autoApprove = autoApprove;
    }

    public int getTtlSeconds() {
        return ttlSeconds;
    }

    public void setTtlSeconds(int ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }

    public int getMaxBatchCount() {
        return maxBatchCount;
    }

    public void setMaxBatchCount(int maxBatchCount) {
        this.maxBatchCount = maxBatchCount;
    }
}
