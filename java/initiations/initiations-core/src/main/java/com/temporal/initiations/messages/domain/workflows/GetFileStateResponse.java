package com.temporal.initiations.messages.domain.workflows;

import java.util.ArrayList;
import java.util.List;

public class GetFileStateResponse {
    private FileInfo fileInfo;
    public List<String> errors = new ArrayList<>();
    private VerifyEntitlementsResponse entitlements;
    private InitiateFileRequest args;
    private InitiateFileRequestExecutionOptions executionOptions;
    private ApproveFileRequest approval;
    private SendLevel1Response level1Psr;
    private BatchFileResponse batches;
    private GetCustomerPreferencesResponse preferences;
    private PersistTransformedFileResponse transformedFile;
    private FileCheckResponse fileCheck;
    private boolean isCancelled;
    public GetFileStateResponse() {
    }

    public InitiateFileRequest getArgs() {
        return args;
    }

    public void setArgs(InitiateFileRequest args) {
        this.args = args;
    }

    public void setApproval(ApproveFileRequest approval) {
        this.approval = approval;
    }

    public ApproveFileRequest getApproval() {
        return approval;
    }

    public VerifyEntitlementsResponse getEntitlements() {
        return entitlements;
    }

    public void setEntitlements(VerifyEntitlementsResponse entitlements) {
        this.entitlements = entitlements;
    }


    public void setLevel1Psr(SendLevel1Response level1Psr) {
        this.level1Psr = level1Psr;
    }

    public SendLevel1Response getLevel1Psr() {
        return level1Psr;
    }

    public void setBatches(BatchFileResponse batches) {
        this.batches = batches;
    }

    public BatchFileResponse getBatches() {
        return batches;
    }

    public GetCustomerPreferencesResponse getPreferences() {
        return preferences;
    }
    public void setPreferences(GetCustomerPreferencesResponse preferences) {
        this.preferences = preferences;
    }

    public void setTransformedFile(PersistTransformedFileResponse persistTransformedFileResponse) {
        this.transformedFile = persistTransformedFileResponse;
    }

    public PersistTransformedFileResponse getTransformedFile() {
        return transformedFile;
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }

    public void setFileInfo(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
    }

    public void setFileCheck(FileCheckResponse fileCheckResponse) {
        this.fileCheck = fileCheckResponse;
    }

    public FileCheckResponse getFileCheck() {
        return fileCheck;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public void isCancelled(boolean timedOut) {
        isCancelled = timedOut;
    }

    public InitiateFileRequestExecutionOptions getExecutionOptions() {
        return executionOptions;
    }

    public void setExecutionOptions(InitiateFileRequestExecutionOptions executionOptions) {
        this.executionOptions = executionOptions;
    }
}
