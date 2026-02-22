package com.temporal.initiations.messages.domain.workflows;

import java.util.List;

public class BatchFileRequest {
    public BatchFileRequest() {
    }

    public BatchFileRequest(FileInfo fileInfo, List<String> batchIds, GetCustomerPreferencesResponse preferences) {
        this.fileInfo = fileInfo;
        this.batchIds = batchIds;
        this.preferences = preferences;
    }

    private FileInfo fileInfo;
    private List<String> batchIds = new java.util.ArrayList<>();
    private GetCustomerPreferencesResponse preferences;

    public List<String> getBatchIds() {
        return batchIds;
    }

    public void setBatchIds(List<String> batchIds) {
        this.batchIds = batchIds;
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
}
