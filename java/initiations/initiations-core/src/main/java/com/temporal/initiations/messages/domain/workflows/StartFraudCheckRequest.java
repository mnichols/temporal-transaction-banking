package com.temporal.initiations.messages.domain.workflows;

public class StartFraudCheckRequest {
    private FileInfo fileInfo;
    private String batchId;

    public StartFraudCheckRequest() {
    }

    public StartFraudCheckRequest(FileInfo fileInfo, String batchId) {
        this.fileInfo = fileInfo;
        this.batchId = batchId;
    }
}
