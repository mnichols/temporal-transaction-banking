package com.temporal.initiations.messages.domain.workflows;

import com.fasterxml.classmate.AnnotationOverrides;

import java.util.ArrayList;
import java.util.List;

public class FileStateResponse {
    public List<String> errors = new ArrayList<>();
    private PersistFileResponse file;
    private VerifyEntitlementsResponse entitlements;
    private InitiateFileRequest args;
    private ApproveFileRequest approval;
    private SendAckResponse ack;
    private BatchFileResponse batches;

    public FileStateResponse() {
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

    public PersistFileResponse getFile() {
        return file;
    }

    public void setFile(PersistFileResponse file) {
        this.file = file;
    }

    public void setAck(SendAckResponse ack) {
        this.ack = ack;
    }

    public SendAckResponse getAck() {
        return ack;
    }

    public void setBatches(BatchFileResponse batches) {
        this.batches = batches;
    }

    public BatchFileResponse getBatches() {
        return batches;
    }

}
