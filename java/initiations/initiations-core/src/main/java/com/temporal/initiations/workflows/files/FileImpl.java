package com.temporal.initiations.workflows.files;

import com.temporal.initiations.messages.domain.workflows.*;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.failure.ActivityFailure;
import io.temporal.failure.ApplicationFailure;
import io.temporal.failure.TimeoutFailure;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInit;

public class FileImpl implements File {

    private final FileStateResponse state;
    private final BatchActivities batches;
    private final FileActivities files;
    private final EntitlementActivities entitlements;
    private final NotificationActivities notifications;

    @WorkflowInit
    public FileImpl(InitiateFileRequest args) {
        this.state = new FileStateResponse();
        this.state.setArgs(args);
        this.batches = Workflow.newActivityStub(BatchActivities.class, ActivityOptions.newBuilder().build());
        this.files = Workflow.newActivityStub(FileActivities.class, ActivityOptions.newBuilder().build());
        this.entitlements = Workflow.newActivityStub(
                EntitlementActivities.class,
                // current behavior is to retry 3 times
                // but prefer to fail based on time with ScheduleToCloseTimeout setting...
                ActivityOptions.newBuilder().setRetryOptions(RetryOptions.newBuilder().setMaximumAttempts(3).build()).build());
        this.notifications = Workflow.newActivityStub(NotificationActivities.class, ActivityOptions.newBuilder().build());

    }
    @Override
    public void execute(InitiateFileRequest args) {
        try {
            state.setEntitlements(entitlements.verifyEntitlements(new VerifyEntitlementsRequest()));
        } catch (ActivityFailure e) {
            if((e.getCause() instanceof TimeoutFailure) || ((ApplicationFailure)e.getCause()).getType().equals(Errors.SERVICE_UNAVAILABLE.name())) {
                state.errors.add(e.getMessage());
                return;
            }
        }
        if(state.getEntitlements().isUnauthorized()) {
            // we silently fail the file initiation if the user cannot perform the work
            return;
        }

        state.setFile(files.persistFile(new PersistFileRequest(args.fileId(), args.filePath())));
        state.setAck(notifications.sendAck(new SendAckRequest()));
        state.setBatches(batches.batchFile(new BatchFileRequest(args.fileId())));
        if(state.isApproved()) {
            // broadcast approvals to all related Batches (Workflows)
            batches.approveBatches(new ApproveBatchesRequest(state.getBatches().batchKeys()));
        }

    }

    @Override
    public void approve(ApproveFileRequest cmd) {
        this.state.setApproval(cmd);
    }
}
