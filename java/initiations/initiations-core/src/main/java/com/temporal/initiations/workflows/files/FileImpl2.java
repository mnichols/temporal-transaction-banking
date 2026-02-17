package com.temporal.initiations.workflows.files;

import com.temporal.initiations.messages.domain.workflows.*;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.failure.ActivityFailure;
import io.temporal.failure.ApplicationFailure;
import io.temporal.failure.TimeoutFailure;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInit;

import java.util.Objects;

public class FileImpl2 implements File {

    private final FileStateResponse state;
    private final BatchActivities batches;
    private final FileActivities files;
    private final EntitlementActivities entitlements;
    private final NotificationActivities notifications;
    private final PersistenceActivities persistence;

    @WorkflowInit
    public FileImpl2(InitiateFileRequest args) {
        this.state = new FileStateResponse();
        this.state.setArgs(args);
        this.batches = Workflow.newActivityStub(BatchActivities.class, ActivityOptions.newBuilder().build());
        this.files = Workflow.newActivityStub(FileActivities.class, ActivityOptions.newBuilder().build());
        this.persistence = Workflow.newActivityStub(PersistenceActivities.class, ActivityOptions.newBuilder().build());
        this.entitlements = Workflow.newActivityStub(
                EntitlementActivities.class,
                // the current behavior is to retry 3 times
                // but prefer to fail based on time with ScheduleToCloseTimeout setting...
                ActivityOptions.newBuilder().setRetryOptions(RetryOptions.newBuilder().setMaximumAttempts(3).build()).build());
        this.notifications = Workflow.newActivityStub(NotificationActivities.class, ActivityOptions.newBuilder().build());
    }
    @Override
    public void execute(InitiateFileRequest args) {

        // Start FileNotification concerns::FileNotifications options:
        // 1. Batch progress monitor: Start a long-running activity that monitors a external storage for batch completion notifications. Send a signal back to this WF periodically or when all batches are done.
        // 2. Notifications context: Start a WF in the `notifications` namespace for receiving commands/events, aggregating them, and then broadcasting to listeners based on rules related to the entity.


        // 1. if transformation to pain.116 is in fact a bottleneck, we should have utilities that can extract the entitlement arguments from arbitrary file types
        // 2. we can have each Batch do transform on the file data if we can solve access to the same file data reads. This lets us get concurrency for free (transform-per-batch-wf)

        // transform file and receive the batchKeys
        files.transformFile(new TransformFileRequest(args.fileId(), args.filePath(), "pain.116.001.03"));
        // question here is whether we want to store records into DB
        persistence.persistFile(new PersistFileRequest(args.fileId(), args.filePath()));

        try {
            // check entitlements
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
        // send our ack ASAP
        state.setAck(notifications.sendAck(new SendAckRequest()));
        // actually START the batches here
        state.setBatches(batches.batchFile(new BatchFileRequest(args.fileId())));
        if(!Objects.isNull(state.getApproval())) {
            // broadcast approvals to all related Batches (Workflows)
            batches.approveBatches(new ApproveBatchesRequest(state.getBatches().batchKeys()));
        }

    }

    @Override
    public void approve(ApproveFileRequest cmd) {
        this.state.setApproval(cmd);
    }
}
