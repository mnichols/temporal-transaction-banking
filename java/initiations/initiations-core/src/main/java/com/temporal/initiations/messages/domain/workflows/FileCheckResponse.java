package com.temporal.initiations.messages.domain.workflows;

public class FileCheckResponse {
    private boolean isFailed;

    public FileCheckResponse() {
    }

    public boolean isFailed() {
        return isFailed;
    }

    public void setFailed(boolean failed) {
        this.isFailed = failed;
    }
}
