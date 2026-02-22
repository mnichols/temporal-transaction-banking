package com.temporal.initiations.messages.domain.workflows;

import jakarta.validation.constraints.NotBlank;

public final class PersistTransformedFileRequest {
    private FileInfo fileInfo;

    public PersistTransformedFileRequest() {
    }

    public PersistTransformedFileRequest(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }

    public void setFileInfo(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
    }
}
