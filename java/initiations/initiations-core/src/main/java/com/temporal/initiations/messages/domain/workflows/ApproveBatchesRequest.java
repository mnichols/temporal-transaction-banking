package com.temporal.initiations.messages.domain.workflows;

import java.util.List;
import java.util.Objects;

public  class ApproveBatchesRequest {
    private FileInfo fileInfo;
    private List<String> batchIds = new java.util.ArrayList<>();

    public ApproveBatchesRequest() {
    }

    public ApproveBatchesRequest(FileInfo fileInfo, List<String> batchIds) {
        this.fileInfo = fileInfo;
        this.batchIds = batchIds;
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }

    public void setFileInfo(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
    }

    public List<String> getBatchIds() {
        return batchIds;
    }

    public void setBatchIds(List<String> batchIds) {
        this.batchIds = batchIds;
    }
}
