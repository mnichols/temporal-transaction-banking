package com.temporal.initiations.workflows.files.activities;

import com.temporal.initiations.messages.domain.workflows.*;
import com.temporal.initiations.workflows.files.Batch;
import io.temporal.activity.Activity;
import io.temporal.api.enums.v1.WorkflowIdConflictPolicy;
import io.temporal.api.enums.v1.WorkflowIdReusePolicy;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;

@Component( "file-initiation-activities")
public class FileActivitiesImpl implements
        EntitlementActivities,
        FileCheckActivities,
        InitiateFileActivities,
        PaymentStatusReportActivities,
        PreferencesActivities,
        ProcessingActivities,
        TransformationActivities
{
    private Logger logger = Workflow.getLogger(FileActivitiesImpl.class);
    @Override
    public BatchFileResponse batchFile(BatchFileRequest cmd) {
        // for each batchId in cmd.getBatchIds()
            // Start BatchWorkflow with
            // WorkflowIdReusePolicy.AllowDuplicateFailedOnly.
            //  - Only batches that failed to execute correctly should be reprocessed.
            // WorkflowIdConflictPolicy.UseExisting
            //  - Protect idempotency without handling dupe errors for control flow.
            //  - You could get a duplicate attempt if this Activity fails while executing and is rescheduled for retry.
        // we could split the batchIds into chunks and start the workflows concurrently if we like (threading)
        var ctx = Activity.getExecutionContext();
        var client = Activity.getExecutionContext().getWorkflowClient();
        for (String batchId : cmd.getBatchIds()) {
            var wf = client.newWorkflowStub(Batch.class,
                    WorkflowOptions.newBuilder()
                            .setWorkflowId(batchId)
                            .setWorkflowIdReusePolicy(WorkflowIdReusePolicy.WORKFLOW_ID_REUSE_POLICY_ALLOW_DUPLICATE_FAILED_ONLY)
                            .setWorkflowIdConflictPolicy(WorkflowIdConflictPolicy.WORKFLOW_ID_CONFLICT_POLICY_USE_EXISTING)
                            .setTaskQueue(ctx.getInfo().getActivityTaskQueue())
                            .build() );
            // TODO catch errors and store for response
            // Start workflows (non-blocking)
            var exec = WorkflowClient.start(wf::execute, new ProcessBatchRequest(
                    Instant.now(Clock.systemUTC()),
                    batchId,
                    cmd.getFileInfo(),
                    cmd.getPreferences(),
                    new ProcessBatchExecutionOptions()
            ));
            logger.info("Started workflow: {}", exec.getWorkflowId());
        }
        return null;
    }

    @Override
    public ApproveBatchesResponse approveBatches(ApproveBatchesRequest cmd) {
        // broadcast a `approveBatch` signal to all the batches
        return null;
    }

    @Override
    public VerifyEntitlementsResponse verifyEntitlements(VerifyEntitlementsRequest cmd) {
        return null;
    }

    @Override
    public PersistTransformedFileResponse persistTransformedFile(PersistTransformedFileRequest cmd) {

        return null;
    }

    @Override
    public SendLevel1Response sendLevel1(SendLevel1Request cmd) {

        return null;
    }

    @Override
    public GetCustomerPreferencesResponse getCustomerPreferences(GetCustomerPreferencesRequest cmd) {

        return null;
    }

    @Override
    public FileCheckResponse checkFile(FileCheckRequest cmd) {
        return null;
    }

    @Override
    public GetInitiateFileExecutionOptionsResponse getOptions(GetInitiateFileExecutionOptionsRequest request) {
        return null;
    }
}
