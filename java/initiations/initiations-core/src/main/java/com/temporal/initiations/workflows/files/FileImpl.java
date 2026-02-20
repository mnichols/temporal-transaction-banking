package com.temporal.initiations.workflows.files;

import com.temporal.initiations.messages.domain.workflows.*;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.failure.ActivityFailure;
import io.temporal.failure.ApplicationFailure;
import io.temporal.failure.TimeoutFailure;
import io.temporal.workflow.*;

import java.time.Duration;
import java.util.Objects;

public class FileImpl implements File {

    private final GetFileStateResponse state;
    private final BatchActivities batches;
    private final FileActivities files;
    private final EntitlementActivities entitlements;
    private final PaymentStatusReportActivities psr;
    private final PreferencesActivities preferences;

    @WorkflowInit
    public FileImpl(InitiateFileRequest args) {
        this.state = new GetFileStateResponse();
        this.state.setFileInfo(args.getFileInfo());
        this.state.setArgs(args);
        this.batches = Workflow.newActivityStub(BatchActivities.class, ActivityOptions.newBuilder().build());
        this.files = Workflow.newActivityStub(FileActivities.class, ActivityOptions.newBuilder().build());
        this.preferences = Workflow.newActivityStub(PreferencesActivities.class, ActivityOptions.newBuilder().build());
        this.entitlements = Workflow.newActivityStub(
                EntitlementActivities.class,
                // the current behavior is to retry 3 times
                // but prefer to fail based on time with ScheduleToCloseTimeout setting...
                ActivityOptions.newBuilder().setRetryOptions(RetryOptions.newBuilder().setMaximumAttempts(3).build()).build());
        this.psr = Workflow.newActivityStub(PaymentStatusReportActivities.class, ActivityOptions.newBuilder().build());
    }
    private String getDetails() {
        String details = """
            1. Start **File** Workflow with `ProcessFileRequest` (above) \s
               1. `WorkflowId = FileId` \s
            2. Check entitlements (API) \s
               1. Send L1 PSR \s
               2. **STOP** if fail \s
            3. Fetch customer PSR preferences (API) \s
            4. Fetch customer Approval preferences (API) \s
            5. Transform **entire** file to XML ( `pain.116.001.03` ) canonical (In-Memory) \s
            6. Within a single Database transaction: \s
               1. For each transaction \s
                  1. create **Payment**  \s
                  2. executeBusinessRules (Drools) (In-Memory) \s
                     1. Note that this computes the **BatchKey** \s
                  3. insert **Payment** (Database) \s
               2. Commit transformed file and all transactions to Database \s
            7. Start Batch Processing \s
               1. `SELECT *,... GROUP BY BatchKey WHERE FileID={request.file_id}` \s
               2. Start **Batch** Workflow per BatchKey (verifying size) \s
                  1. For each **Batch** \s
                     1. `WorkflowId = BatchID` \s
                     2. Perform Fraud Check with GFD \s
                     3. Wait for approval \s
                     4. Transmit to GPO \s
                  3. *Return \\[BatchID\\]* \s
            8. File Dupe Check (DB) \s
               1. This is based on contents of file, not a simple identifier \s
               2. But check with Prasad for feasibility of a hash to get rid of this \s
            9. Control Sum Check (In-Memory) \s
            10. Multi Region File Check (Mix File) (NoOp) \s
                1. This is likely an in-memory check, but do not get hung up here. \s
            11. Wait for approval \s
                1. IF customer preferences are set to "FILE" \s
                2. When approved, Broadcast Approval to all Batches
            """;
        return details;
    }
    @Override
    public void execute(InitiateFileRequest args) {

        // just a simple way to document the Workflow for the UI
        Workflow.setCurrentDetails(getDetails());
        CancellationScope scope = Workflow.newCancellationScope(() -> {
            var entitlementsExec = Async.function(entitlements::verifyEntitlements, new VerifyEntitlementsRequest(this.state.getArgs().getSenderId()));
            var fileCheckExec = Async.function(files::checkFile, new FileCheckRequest(this.state.getFileInfo()));
            try {
                state.setEntitlements(entitlementsExec.get());
            } catch (ActivityFailure e) {
                if((e.getCause() instanceof TimeoutFailure) || ((ApplicationFailure)e.getCause()).getType().equals(Errors.SERVICE_UNAVAILABLE.name())) {
                    state.errors.add(e.getMessage());
                }
            }

            try {
                state.setFileCheck(fileCheckExec.get());
            } catch (ActivityFailure e) {
                if((e.getCause() instanceof TimeoutFailure) || ((ApplicationFailure)e.getCause()).getType().equals(Errors.SERVICE_UNAVAILABLE.name())) {
                    state.errors.add(e.getMessage());
                }
            }
        });
        // block on concurrent validations to be completed
        scope.run();

        // if entitlements or file checks fail, we send L1 PSR and return early
        if(!state.errors.isEmpty() || state.getEntitlements().isUnauthorized() || state.getFileCheck().isFailed()) {
            // TODO determine message contract
            this.state.setLevel1Psr(psr.sendLevel1(new SendLevel1Request()));
            // early return from File Initiation due to entitlements failure
            return;
        }

        this.state.setPreferences(preferences.getCustomerPreferences(new GetCustomerPreferencesRequest()));

        // always persist the transformed canonical file
        state.setTransformedFile(files.persistTransformedFile(new PersistTransformedFileRequest(args.getFileInfo())));

        state.setBatches(batches.batchFile(new BatchFileRequest(this.state.getFileInfo(), this.state.getTransformedFile().getBatchIds())));

        // send our ack ASAP
        state.setLevel1Psr(psr.sendLevel1(new SendLevel1Request()));

        if(!Objects.isNull(state.getApproval())) {
            // broadcast approvals to all related Batches (Workflows)
            batches.approveBatches(new ApproveBatchesRequest(state.getFileInfo(), state.getTransformedFile().getBatchIds()));
        }
        if(this.state.getPreferences().isFileApprovalRequired()) {
            // determine TTL for approval time
            Workflow.await(Duration.ofSeconds(864000), () -> this.state.getApproval() != null);
            this.batches.approveBatches(new ApproveBatchesRequest(this.state.getFileInfo(), this.state.getTransformedFile().getBatchIds()));
        }

    }

    @Override
    public void approve(ApproveFileRequest cmd) {
        this.state.setApproval(cmd);
    }
}
