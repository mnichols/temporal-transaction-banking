# Temporal Workflows

## FileWorkflow

**Purpose**: Orchestrate the processing of a complete payment initiation file.

**Interface**: `File` in `com.temporal.initiations.workflows` package
- **Workflow Method**: Accepts `InitiateFileRequest` containing fileId and submitterId
- **Signal Method**: `approve()` - Approves the file for processing

**Execution Steps**:

1. **Retrieve and Validate File**
   - Retrieve the file content from storage
   - Verify submitter is authorized to process files
   - Validate file format is valid PAIN.001.001.03
   - Validate file passes schema validation
   - Ensure file contains at least one payment record
   - Throws `ValidationException` if any check fails (workflow fails)

2. **Transform File**
   - Convert PAIN-113 to PAIN-116 format
   - Extract payment records
   - Map field values
   - Generate batch keys based on payment attributes (currency, destination, etc.)
   - Group payments into `Batch` objects

3. **Persist Batches**
   - Save each batch record to the database
   - Mark batches as "awaiting approval"

4. **Create Batch Workflows**
   - Create a child batch workflow for each batch
   - Workflow ID is the batch key (hash of batch criteria)
   - Each batch workflow starts independently

5. **Send Acknowledgment**
   - Notify the submitter that file has been received
   - Report number of batches created
   - Indicate approval is required to proceed

6. **Await Approval Signal**
   - Workflow pauses and waits for approval
   - Can receive `approve()` signal via Temporal API
   - Has 24-hour timeout (configurable)

7. **Complete**
   - When approval signal is received, batches proceed to downstream processing
   - If timeout expires, workflow fails (can be retried)

---

## BatchWorkflow

**Purpose**: Orchestrate the processing of a single batch of payments.

**Execution Steps**:

1. **Await Approval Signal**
   - Workflow pauses and waits for approval signal from File workflow
   - Or 24-hour timeout
   - Receives `approve()` signal to proceed

2. **Fraud Detection Check**
   - Analyze payment patterns
   - Check against fraud rules
   - Return risk score and decision
   - If fraud check fails: Workflow fails, batch is marked for review
   - If fraud check passes: Continue to next step

3. **Route/Transmit Downstream**
   - Route batch based on batch criteria
   - Transmit in PAIN.116 format
   - Record transmission details
   - Update batch status to "transmitted"

4. **Complete**
   - Workflow completes successfully when batch is transmitted

---

## Signal Coordination

### File Workflow Signals

**`approve()` signal**:
- Sender: External approval system or UI
- Receiver: FileWorkflow
- Effect: Allows file workflow to proceed to signaling batch workflows for approval

**Triggered by**: Manual approval, automated rules, or system integration

### Child Workflow (Batch) Coordination

When FileWorkflow receives approval, it signals all child BatchWorkflows to proceed. Each BatchWorkflow can proceed independently.

---

## Error Handling

### Workflow Failures

**File Validation Fails**:
- Entire workflow fails immediately
- File is rejected
- Submitter is notified of validation error

**Entitlement Check Fails**:
- Workflow fails
- Audit log records rejection
- Submitter is notified

**Activity Failures**:
- Automatic retry (default: 3 times with exponential backoff)
- If all retries fail, workflow fails

**Timeout**:
- If approval signal not received within 24 hours
- Workflow can be configured to auto-fail or continue

### Batch Workflow Failures

**Fraud Check Fails**:
- Batch workflow fails
- Batch is marked for manual review
- Monitoring/alerting notifies operations team

**Transmission Fails**:
- Activity retries
- If all retries fail, batch workflow fails
- Batch remains pending transmission

---

## Monitoring & Observability

### Temporal UI

Access at http://localhost:8080 (when Temporal server is running):

1. **Workflows** tab - View all running workflows
   - Filter by namespace: `initiations`
   - View workflow execution history
   - Inspect input/output

2. **File Workflow**:
   - Execution timeline showing each activity
   - Input file ID and content
   - Output batch IDs created
   - Pending approval signal

3. **Batch Workflows**:
   - File-Related workflow execution details
   - Fraud check results
   - Transmission status

### Metrics

Via `/actuator/metrics` endpoint:
- Workflow execution duration
- Activity execution time
- Retry counts
- Batch counts
- Signal received timestamps

### Logs

Application logs (stdout/stderr):
```
2026-01-25 10:15:30 INFO  FileWorkflow: Processing file=test-001
2026-01-25 10:15:31 INFO  Activity: File validation passed
2026-01-25 10:15:32 INFO  Activity: Transformed file into 5 batches
2026-01-25 10:15:40 INFO  Activity: Batch persistence completed
2026-01-25 10:15:45 INFO  BatchWorkflow: Batch batches/curr-1 waiting for approval
2026-01-25 10:16:00 INFO  FileWorkflow: File awaiting approval signal
```

---

## Testing Workflows

Temporal workflows should be tested using Temporal's test frameworks:

- **Activity Testing**: Test individual activity implementations with `TestActivityEnvironment`
- **Workflow Testing**: Test complete workflow execution with `TestWorkflowEnvironment` to simulate signal delivery, timeouts, and activity results
- **Integration Testing**: Test workflows with actual workers to verify end-to-end execution

See source code in `java/initiations/` modules for test implementation examples.

---

## Configuration

**Timeout Settings**:
- **File Workflow Execution Timeout**: 24 hours (time to wait for approval)
- **Batch Workflow Execution Timeout**: 24 hours
- **Activity Execution Timeout**: Configurable per activity
- **Heartbeat Timeout**: Configurable per activity

**Retry Policy**:
- Activities have automatic retry with exponential backoff
- Initial interval: 1 second
- Max interval: 100 seconds
- Backoff coefficient: 2.0
- Max attempts: 3 (configurable)

Configuration is managed through `application.yaml` in each deployable module.
