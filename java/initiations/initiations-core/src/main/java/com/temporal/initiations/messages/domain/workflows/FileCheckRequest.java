package com.temporal.initiations.messages.domain.workflows;

public class FileCheckRequest {
    private FileInfo fileInfo;

    public FileCheckRequest() {
    }

    public FileCheckRequest(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }

    public void setFileInfo(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
    }
}
