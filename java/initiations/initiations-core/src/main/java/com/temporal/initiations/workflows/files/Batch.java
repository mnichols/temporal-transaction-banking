package com.temporal.initiations.workflows.files;

import com.temporal.initiations.messages.domain.workflows.ApproveBatchRequest;
import com.temporal.initiations.messages.domain.workflows.ApproveFileRequest;
import com.temporal.initiations.messages.domain.workflows.InitiateBatchRequest;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface Batch {
    @WorkflowMethod
    void execute(InitiateBatchRequest args);

    @SignalMethod
    void approve(ApproveBatchRequest cmd);
}
