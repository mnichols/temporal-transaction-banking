package com.temporal.initiations.messages.domain.workflows;

import java.util.Objects;

public final class InitiateFileRequestExecutionOptions {
    private boolean autoApprove;

    public boolean isAutoApprove() {
        return autoApprove;
    }

    public void setAutoApprove(boolean autoApprove) {
        this.autoApprove = autoApprove;
    }
}
