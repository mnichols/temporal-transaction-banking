package com.temporal.initiations.messages.domain.workflows;

public class FileStateResponse {
    private InitiateFileRequest args;
    private ApproveFileRequest approval;

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
}
