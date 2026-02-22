package com.temporal.initiations.workflows.files;

import com.temporal.initiations.messages.domain.workflows.*;
import com.temporal.initiations.workflows.files.activities.FraudActivities;
import com.temporal.initiations.workflows.files.activities.TransformationActivities;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInit;

import java.time.Duration;

public class BatchImpl implements Batch {
    private GetBatchStateResponse state;

    @WorkflowInit
    public BatchImpl(ProcessBatchRequest args) {
        this.state = new GetBatchStateResponse(args, null);
    }
    @Override
    public void execute(ProcessBatchRequest args) {
        var fraudStart = Workflow.newActivityStub(FraudActivities.class, ActivityOptions.newBuilder()
                .setScheduleToCloseTimeout(Duration.ofSeconds(90)).build());

        this.state.setStartFraudCheck(fraudStart.startFraudCheck(new StartFraudCheckRequest()));

        // poll the file every 45 seconds for completion using Temporal server as our throttling mechanism
        var completeFraud = Workflow.newActivityStub(FraudActivities.class, ActivityOptions.newBuilder()
                .setRetryOptions(
                        RetryOptions.newBuilder()
                                .setInitialInterval(Duration.ofSeconds(45))
                                .setBackoffCoefficient(1)
                                .build())
                .build());
        this.state.setFraudCheck(completeFraud.completeFraudCheck(new CompleteFraudCheckRequest(
                this.state.getArgs().getBatchId(),
                this.state.getArgs().getFileInfo(),
                this.state.getStartFraudCheck().getFraudCheckFilePath())));

        Workflow.await(() -> this.state.getApproval() != null);
    }

    @Override
    public void approveBatch(ApproveBatchRequest cmd) {
        this.state.setApproval(cmd);
    }

    @Override
    public GetBatchStateResponse getState() {
        return this.state;
    }

}
