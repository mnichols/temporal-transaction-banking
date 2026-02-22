package com.temporal.initiations.messages.domain.workflows;

public final class GetBatchStateResponse {
    private  ProcessBatchRequest args;
    private  ApproveBatchRequest approval;
    private StartFraudCheckResponse startFraudCheck;
    private CompleteFraudCheckResponse fraudCheck;
    public GetBatchStateResponse() {
    }

    public GetBatchStateResponse(ProcessBatchRequest args, ApproveBatchRequest approval) {
        this.args = args;
        this.approval = approval;
    }

    public ProcessBatchRequest getArgs() {
        return args;
    }

    public void setArgs(ProcessBatchRequest args) {
        this.args = args;
    }

    public ApproveBatchRequest getApproval() {
        return approval;
    }

    public void setApproval(ApproveBatchRequest approval) {
        this.approval = approval;
    }

    public CompleteFraudCheckResponse getFraudCheck() {
        return fraudCheck;
    }

    public void setFraudCheck(CompleteFraudCheckResponse fraudCheck) {
        this.fraudCheck = fraudCheck;
    }

    public StartFraudCheckResponse getStartFraudCheck() {
        return startFraudCheck;
    }

    public void setStartFraudCheck(StartFraudCheckResponse startFraudCheck) {
        this.startFraudCheck = startFraudCheck;
    }
}
