package com.temporal.initiations.messages.domain.workflows;

public class CompleteFraudCheckRequest {
    private String batchId;
    private FileInfo fileInfo;
    private String fraudCheckFilePath;
    public CompleteFraudCheckRequest() {
    }

    public CompleteFraudCheckRequest(String batchId, FileInfo fileInfo, String fraudCheckFilePath) {
        this.batchId = batchId;
        this.fileInfo = fileInfo;
        this.fraudCheckFilePath = fraudCheckFilePath;
    }

    public String getFraudCheckFilePath() {
        return fraudCheckFilePath;
    }

    public void setFraudCheckFilePath(String fraudCheckFilePath) {
        this.fraudCheckFilePath = fraudCheckFilePath;
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }

    public void setFileInfo(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }
}
