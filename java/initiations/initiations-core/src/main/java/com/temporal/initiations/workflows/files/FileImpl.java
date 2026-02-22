package com.temporal.initiations.workflows.files;

import com.temporal.initiations.messages.domain.workflows.*;
import com.temporal.initiations.workflows.files.activities.*;
import io.temporal.activity.ActivityOptions;
import io.temporal.activity.LocalActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.failure.ActivityFailure;
import io.temporal.failure.ApplicationFailure;
import io.temporal.failure.TimeoutFailure;
import io.temporal.workflow.*;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.Objects;

public class FileImpl implements File {

    private final InitiateFileActivities initiations;
    private Logger logger = Workflow.getLogger(FileImpl.class);
    private final FileCheckActivities files;
    private final GetFileStateResponse state;
    private final ProcessingActivities processing;
    private final TransformationActivities transformations;
    private final EntitlementActivities entitlements;
    private final PaymentStatusReportActivities psr;
    private final PreferencesActivities preferences;

    @WorkflowInit
    public FileImpl(InitiateFileRequest args) {
        this.state = new GetFileStateResponse();
        this.state.setFileInfo(args.getFileInfo());
        this.state.setArgs(args);
        this.state.setExecutionOptions(args.getExecutionOptions());

        this.entitlements = Workflow.newActivityStub(
                EntitlementActivities.class,
                // the current behavior is to retry 3 times
                // but prefer to fail based on time with ScheduleToCloseTimeout setting...
                ActivityOptions.newBuilder().setRetryOptions(RetryOptions.newBuilder().setMaximumAttempts(3).build()).build());
        this.files = Workflow.newLocalActivityStub(FileCheckActivities.class, LocalActivityOptions.newBuilder().build());
        this.initiations = Workflow.newLocalActivityStub(InitiateFileActivities.class,
                LocalActivityOptions.newBuilder().setScheduleToCloseTimeout(Duration.ofSeconds(5)).build());
        this.preferences = Workflow.newActivityStub(PreferencesActivities.class, ActivityOptions.newBuilder().build());
        this.processing = Workflow.newActivityStub(ProcessingActivities.class, ActivityOptions.newBuilder().build());
        this.psr = Workflow.newActivityStub(PaymentStatusReportActivities.class, ActivityOptions.newBuilder().build());
        this.transformations = Workflow.newActivityStub(TransformationActivities.class, ActivityOptions.newBuilder().build());
    }
    private String getDetails() {
        String details = """
            1. Start **File** Workflow with `ProcessFileRequest` (above) \s
               1. `WorkflowId = FileId` \s
            2. Perform File And Entitlement Checks (Concurrently) \s
               1. *File checks* \s
                  1. File Dupe Check (DB) \s
                     1. This is based on contents of file, not a simple identifier \s
                     2. File has a hash that is used for this comparis \s
                  2. Control Sum Check (In-Memory) \s
                  3. Multi Region File Check (Mix File) (NoOp) \s
                     1. This is likely an in-memory check, but do not get hung up here. \s
               2. *Entitlements check* \s
                  1. Check SenderID entitlements (API) \s
               3. Send L1 PSR \s
               4. **STOP** if Any Checks Fail \s
            3. Fetch customer PSR preferences (API) \s
            4. Fetch customer Approval preferences (API) \s
            5. Transform **entire** file to XML ( `pain.116.001.03` ) canonical (In-Memory) \s
            6. Within a single Database transaction: \s
               1. For each transaction \s
                  1. create **Payment** \s
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
            8. Wait for approval \s
               1. IF customer preferences are set to "FILE" \s
               2. When approved, Broadcast Approval to all Batches
            """;
        return details;
    }
    @Override
    public void execute(InitiateFileRequest args) {

        // just a simple way to document the Workflow for the UI
        Workflow.setCurrentDetails(getDetails());

        // get default execution options from environment/config
        // while allowing to merge options passed in from caller
        // this is handy for testing or other execution contexts
        this.state.setExecutionOptions(
                this.initiations.getOptions(
                        new GetInitiateFileExecutionOptionsRequest(args.getExecutionOptions()))
                        .getOptions());

        // start a non-blocking timer to prevent files from forever-processing
        // this just flips a bit when the timer fires to other routines can respond (eg the steps of the transaction)
        Workflow.newCancellationScope((innerScope) -> {
            Async.procedure(() -> {
                var conditionMet = Workflow.await(Duration.ofSeconds(args.getExecutionOptions().getTtlSeconds()), ()->state.getApproval() != null);
                if(conditionMet) {
                    return;
                }
                logger.info("TTL timer fired for File Initiation");

                // ttl Timer fired, so this initiation should be marked as timed out
                state.isCancelled(true);
                // perform some other work to let people know this file is not going to be processed
                // alternatively, you could rerun this WF all over again with ContinueAsNew
            });
            // do ahead and run this...it is non-blocking
            innerScope.run();
        });
        var stepScope = Workflow.newCancellationScope((innerScope) -> {
            if(this.state.isCancelled()) {
                // something cancelled this process so abort early
                innerScope.cancel();
            }
            this.runSteps();
        });

        stepScope.run();
    }

    private void runSteps() {

        // run entitlement and file checks concurrently
        var entitlementsExec = Async.function(entitlements::verifyEntitlements, new VerifyEntitlementsRequest(this.state.getArgs().getSenderId()));
        // File checks include: FileDupeCheck, ControlSumCheck, MultiRegionFileCheck
        var fileCheckExec = Async.function(files::checkFile, new FileCheckRequest(this.state.getFileInfo()));

        // now inspect the check results
        // we can do this sequentially since we want to wait for all to complete but they have different result types
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

        // if entitlements or file checks fail, we send L1 PSR and return early
        if(!state.errors.isEmpty() || state.getEntitlements().isUnauthorized() || state.getFileCheck().isFailed()) {
            // TODO determine message contract for marking this as unprocessable
            this.state.setLevel1Psr(psr.sendLevel1(new SendLevel1Request()));
            // early return from File Initiation due to entitlements failure
            return;
        }

        // we are cleared for takeoff, so do the actual processing work now!

        // let's get the customer preferences to know how we deal with approvals
        this.state.setPreferences(preferences.getCustomerPreferences(new GetCustomerPreferencesRequest()));

        // always persist the transformed canonical file
        state.setTransformedFile(transformations.persistTransformedFile(new PersistTransformedFileRequest(state.getArgs().getFileInfo())));

        // Check that the batch size is not unwieldy. We can only check this after the file is transformed.
        // The file itself is not correctable, so the best we can do is write our error down and
        // Exit the workflow.
        if(state.getTransformedFile().getBatchIds().size() > this.state.getExecutionOptions().getMaxBatchCount()) {
            this.state.errors.add("Batch count exceeds allowed count of " + this.state.getExecutionOptions().getMaxBatchCount());
            // TODO determine message contract for marking this as unprocessable
            this.state.setLevel1Psr(psr.sendLevel1(new SendLevel1Request()));
            return;
        }

        // now that we have the transformed file, we can start processing batches
        // this will fanout into N batches inside the Activity. It's cheap and lightweight
        state.setBatches(processing.batchFile(
                new BatchFileRequest(this.state.getFileInfo(),
                        this.state.getTransformedFile().getBatchIds(),
                        this.state.getPreferences())));

        // send our L1 ACK ASAP
        // with PENDING status
        state.setLevel1Psr(psr.sendLevel1(new SendLevel1Request()));

        if(!Objects.isNull(state.getApproval())) {
            // broadcast approvals to all related Batches (Workflows)
            processing.approveBatches(new ApproveBatchesRequest(state.getFileInfo(), state.getTransformedFile().getBatchIds()));
        }
        if(this.state.getPreferences().isFileApprovalRequired()) {
            // determine TTL for approval time
            Workflow.await(Duration.ofSeconds(864000), () -> this.state.getApproval() != null);
            this.processing.approveBatches(new ApproveBatchesRequest(this.state.getFileInfo(), this.state.getTransformedFile().getBatchIds()));
        }
    }
    @Override
    public void approveFile(ApproveFileRequest cmd) {
        this.state.setApproval(cmd);
    }

    @Override
    public GetFileStateResponse getState() {
        return this.state;
    }
}
