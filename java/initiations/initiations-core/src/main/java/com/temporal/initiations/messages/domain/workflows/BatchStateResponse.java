package com.temporal.initiations.messages.domain.workflows;

import java.util.Objects;

public final class BatchStateResponse {
    private  InitiateBatchRequest args;
    private  ApproveBatchRequest approval;

    public BatchStateResponse() {
    }

    public BatchStateResponse(InitiateBatchRequest args, ApproveBatchRequest approval) {
        this.args = args;
        this.approval = approval;
    }

    public InitiateBatchRequest getArgs() {
        return args;
    }

    public void setArgs(InitiateBatchRequest args) {
        this.args = args;
    }

    public ApproveBatchRequest getApproval() {
        return approval;
    }

    public void setApproval(ApproveBatchRequest approval) {
        this.approval = approval;
    }
}
