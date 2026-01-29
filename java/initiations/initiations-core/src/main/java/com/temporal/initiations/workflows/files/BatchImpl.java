package com.temporal.initiations.workflows.files;

import com.temporal.initiations.messages.domain.workflows.ApproveBatchRequest;
import com.temporal.initiations.messages.domain.workflows.BatchStateResponse;
import com.temporal.initiations.messages.domain.workflows.InitiateBatchRequest;
import com.temporal.initiations.messages.domain.workflows.TransformFileRequest;
import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInit;

public class BatchImpl implements Batch {
    private BatchStateResponse state;
    private FileActivities files;

    @WorkflowInit
    public BatchImpl(InitiateBatchRequest args) {
        this.state = new BatchStateResponse(args, null);
        this.files = Workflow.newActivityStub(FileActivities.class, ActivityOptions.newBuilder().build());
    }
    @Override
    public void execute(InitiateBatchRequest args) {
        this.files.transformFile(new TransformFileRequest(this.state.getArgs().fileId(), this.state.getArgs().filePath(), "pain.116.001.03"));

    }

    @Override
    public void approve(ApproveBatchRequest cmd) {
        this.state.setApproval(cmd);
    }

}
