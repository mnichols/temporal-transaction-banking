package com.temporal.initiations.messages.domain.workflows;

public class GetCustomerPreferencesResponse {
    private boolean isFileAutoApproved;
    private boolean isBatchAutoApproved;
    private boolean isFileApprovalRequired;
    private boolean isBatchApprovalRequired;

    public boolean isBatchApprovalRequired() {
        return isBatchApprovalRequired;
    }

    public void setBatchApprovalRequired(boolean batchApprovalRequired) {
        isBatchApprovalRequired = batchApprovalRequired;
    }

    public boolean isFileApprovalRequired() {
        return isFileApprovalRequired;
    }

    public void setFileApprovalRequired(boolean fileApprovalRequired) {
        isFileApprovalRequired = fileApprovalRequired;
    }

    public boolean isBatchAutoApproved() {
        return isBatchAutoApproved;
    }

    public void setBatchAutoApproved(boolean batchAutoApproved) {
        isBatchAutoApproved = batchAutoApproved;
    }

    public boolean isFileAutoApproved() {
        return isFileAutoApproved;
    }

    public void setFileAutoApproved(boolean fileAutoApproved) {
        isFileAutoApproved = fileAutoApproved;
    }
}
