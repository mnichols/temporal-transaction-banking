package com.temporal.initiations.messages.domain.workflows;

import java.time.Instant;

public class ProcessBatchRequest {
    private Instant timestamp;
    private String batchId;
    private FileInfo fileInfo;
    private GetCustomerPreferencesResponse preferences;
    private ProcessBatchExecutionOptions options;

    public ProcessBatchRequest() {
    }

    public ProcessBatchRequest(Instant timestamp,
                               String batchId,
                               FileInfo fileInfo,
                               GetCustomerPreferencesResponse preferences,
                               ProcessBatchExecutionOptions options) {
        this.batchId = batchId;
        this.fileInfo = fileInfo;
        this.options = options;
        this.preferences = preferences;
        this.timestamp = timestamp;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }

    public void setFileInfo(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
    }

    public GetCustomerPreferencesResponse getPreferences() {
        return preferences;
    }

    public void setPreferences(GetCustomerPreferencesResponse preferences) {
        this.preferences = preferences;
    }

    public ProcessBatchExecutionOptions getOptions() {
        return options;
    }

    public void setOptions(ProcessBatchExecutionOptions options) {
        this.options = options;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
