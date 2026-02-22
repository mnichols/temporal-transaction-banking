package com.temporal.initiations.workflows.files;

import com.temporal.initiations.messages.domain.workflows.*;
import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface Batch {
    @WorkflowMethod
    void execute(ProcessBatchRequest args);

    @SignalMethod
    void approveBatch(ApproveBatchRequest cmd);

    @QueryMethod
    GetBatchStateResponse getState();
}
