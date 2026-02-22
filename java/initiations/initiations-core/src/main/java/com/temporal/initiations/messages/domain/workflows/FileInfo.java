package com.temporal.initiations.messages.domain.workflows;

public class FileInfo {
    private String fileId;
    private String filePath;

    public FileInfo() {
    }

    public FileInfo(String fileId, String filePath) {
        this.fileId = fileId;
        this.filePath = filePath;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
