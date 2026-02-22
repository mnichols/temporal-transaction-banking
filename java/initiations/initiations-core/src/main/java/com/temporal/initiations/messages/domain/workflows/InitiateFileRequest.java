package com.temporal.initiations.messages.domain.workflows;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.Objects;

/**
 * Request to initiate processing of a payment file.
 * <p>
 * This record contains the file identifier, XML content, and submitter information
 * needed to start a File workflow execution. All fields are required and validated
 * to be non-blank.
 *
 */
public class InitiateFileRequest {
    private Instant timestamp;
    private FileInfo fileInfo;
    private String senderId;
    private InitiateFileRequestExecutionOptions executionOptions;

    public InitiateFileRequest() {
    }

    public InitiateFileRequest(Instant timestamp, FileInfo fileInfo, String senderId, InitiateFileRequestExecutionOptions executionOptions) {
        this.timestamp = timestamp;
        this.fileInfo = fileInfo;
        this.senderId = senderId;
        this.executionOptions = executionOptions;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }

    public void setFileInfo(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public InitiateFileRequestExecutionOptions getExecutionOptions() {
        return executionOptions;
    }

    public void setExecutionOptions(InitiateFileRequestExecutionOptions executionOptions) {
        this.executionOptions = executionOptions;
    }
}
