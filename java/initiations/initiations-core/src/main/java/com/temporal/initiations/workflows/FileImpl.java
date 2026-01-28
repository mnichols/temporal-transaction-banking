package com.temporal.initiations.workflows;

import com.temporal.initiations.messages.api.FileWorkflowInput;
import com.temporal.initiations.messages.domain.workflows.ApproveFileRequest;
import com.temporal.initiations.messages.domain.workflows.FileStateResponse;
import com.temporal.initiations.messages.domain.workflows.InitiateFileRequest;
import io.temporal.workflow.WorkflowInit;

public class FileImpl implements File {

    private final FileStateResponse state;

    @WorkflowInit
    public FileImpl(InitiateFileRequest args) {
        this.state = new FileStateResponse();
        this.state.setArgs(args);
    }
    @Override
    public void execute(InitiateFileRequest args) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void approve(ApproveFileRequest cmd) {
        this.state.setApproval(cmd);
    }
}
