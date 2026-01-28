# Temporal Workflows

## FileWorkflow

**Purpose**: Orchestrate the processing of a complete payment initiation file.

**Interface**: `File`
- **Method**: `void execute(InitiateFileRequest args)`
- **Input**: `InitiateFileRequest`
  - `fileId` - Unique file identifier
  - `submitterId` - ID of the requester
- **Signal**: `void approve(ApproveFileRequest cmd)` - Approves the file for processing

**Steps**:

### 1. Retrieve and Validate File

```java
String fileContent = activity.retrieveFile(fileId);
activity.validateFileEntitlement(fileId, submitterId);
activity.validateFileFormat(fileContent);
```

Checks:
- Submitter is authorized to process files
- File format is valid PAIN.001.001.03
- File passes schema validation
- File contains at least one payment record

**Throws**: `ValidationException` if checks fail (workflow fails)

### 2. Transform File

```java
TransformedFile transformed = activity.transformPainFile(fileContent);
```

Converts PAIN-113 to PAIN-116 format:
- Extract payment records
- Map field values
- Generate batch keys based on payment attributes (currency, destination, etc.)
- Create `Batch` objects with grouped payments

**Returns**: `TransformedFile` containing:
- Original file metadata
- List of `Batch` objects with payments grouped by criteria

### 3. Persist Batches

For each batch:

```java
for (Batch batch : transformed.batches) {
    activity.persistBatch(batch);
}
```

Saves batch records to database:
- One database record per batch
- Includes payment records
- Marks as "awaiting approval"

### 4. Create Batch Workflows

For each batch:

```java
String batchKey = calculateBatchKey(batch);
WorkflowOptions options = WorkflowOptions.newBuilder()
    .setWorkflowId(batchKey)
    .build();

BatchWorkflow batchWorkflow = Workflow.newChildWorkflowStub(
    BatchWorkflow.class,
    options
);

batchWorkflow.processBatch(new InitiateBatchRequest(batch));
```

Creates a child workflow for each batch:
- Workflow ID is the batch key (hash of batch criteria)
- Each batch workflow starts independently
- Parent workflow waits for all child workflows to start

### 5. Send Acknowledgment

```java
activity.sendAcknowledgment(fileId, batchCount);
```

Notifies the submitter:
- File has been received and split into batches
- Number of batches created
- File processing is underway
- Approval is required to proceed

### 6. Await Approval Signal

```java
Workflow.await(Duration.ofHours(24), () -> approved);
```

Workflow pauses and waits for:
- `approve` signal to be sent via Temporal API
- 24-hour timeout (configurable)

**Signal Handler**:
```java
@SignalMethod
public void approve(ApproveFileRequest cmd) {
    this.approved = true;
    this.approvalId = cmd.approvalId();
}
```

### 7. Complete

When approval signal is received or timeout expires, workflow completes.

**Success**: Batches are now approved for downstream processing
**Failure**: Workflow fails if approval timeout expires (can be retried)

---

## BatchWorkflow

**Purpose**: Orchestrate the processing of a single batch of payments.

**Input**: `InitiateBatchRequest`
- `batch` - Batch object with grouped payments
- `batchId` - Unique batch identifier

**Steps**:

### 1. Await Approval Signal

```java
Workflow.await(Duration.ofHours(24), () -> approved);
```

Pauses and waits for approval from the File workflow:
- File workflow sends approval signal
- Or 24-hour timeout

**Signal Handler**:
```java
@SignalMethod
public void approve(ApproveFileRequest cmd) {
    this.approved = true;
    this.approvalId = cmd.approvalId();
}
```

### 2. Fraud Detection Check

```java
FraudCheckResult result = activity.performFraudCheck(batch);
```

Performs fraud detection:
- Analyzes payment patterns
- Checks against fraud rules
- Returns risk score and decision

**Returns**: `FraudCheckResult`
- `passed` - Boolean (true if passed fraud check)
- `riskScore` - Numeric risk level
- `reason` - Explanation if rejected

**Behavior**:
- If failed: Workflow fails, batch is marked for review
- If passed: Continue to next step

### 3. Route/Transmit

```java
activity.routeBatchDownstream(batch, fraudResult);
```

Sends batch to downstream processor:
- Routes based on batch criteria
- Transmits in PAIN.116 format
- Records transmission details
- Updates batch status to "transmitted"

**Returns**: Transmission confirmation

### 4. Complete

Workflow completes successfully when batch is transmitted.

---

## Signal Coordination

### File Workflow Signals

**`approve()` signal**:
- Sender: External approval system or UI
- Receiver: FileWorkflow
- Effect: Allows file workflow to proceed to sending approval to batch workflows

**Triggered by**: Manual approval, automated rules, or system integration

```bash
# Example: Signal via Temporal CLI
temporal workflow signal \
  --workflow-id <file-id> \
  --type approve
```

### Child Workflow (Batch) Coordination

When FileWorkflow approves, it signals all child BatchWorkflows:

```java
for (BatchWorkflow batchWf : batchWorkflows) {
    batchWf.approve();
}
```

Each BatchWorkflow can proceed independently.

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
   - Child workflow execution details
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

### Unit Testing Activities

```java
@Test
void testFileValidation() {
    TestActivityEnvironment env = TestActivityEnvironment.newInstance();
    env.registerActivitiesImplementations(new FileActivities());

    FileActivity activity = env.getActivityClient(FileActivity.class);
    assertDoesNotThrow(() -> activity.validateFileEntitlement(validRequest));
}
```

### Testing Workflows with Test Server

```java
@Test
void testFileWorkflowApproval() throws Exception {
    TestWorkflowEnvironment testEnv = TestWorkflowEnvironment.newInstance();
    WorkerFactory workerFactory = testEnv.getWorkerFactory();
    Worker worker = workerFactory.newWorker("initiations");

    worker.registerWorkflowImplementationTypes(FileWorkflowImpl.class);
    worker.registerActivitiesImplementations(new FileActivities());

    testEnv.start();

    FileWorkflow workflow = testEnv.getWorkflowClient().newWorkflowStub(
        FileWorkflow.class
    );

    // Start async
    InitiateFileRequest request = new InitiateFileRequest("file-001", "submitter-123");
    WorkflowClient.start(workflow::execute, request);

    // Send signal
    ApproveFileRequest approvalCmd = new ApproveFileRequest("approval-123");
    workflow.approve(approvalCmd);

    // Verify completion
    assertDoesNotThrow(() -> workflow.getResult());
}
```

---

## Configuration

### Timeout Configuration

**File Workflow Execution Timeout**:
```yaml
# application.yaml
temporal:
  file-workflow:
    timeout-hours: 24  # Time to wait for approval
```

**Batch Workflow Execution Timeout**:
```yaml
temporal:
  batch-workflow:
    timeout-hours: 24
```

**Activity Timeout**:
```yaml
temporal:
  activity:
    timeout-seconds: 300
    start-to-close-timeout-seconds: 300
    heartbeat-timeout-seconds: 60
```

### Retry Policy

```yaml
temporal:
  activity:
    retry-policy:
      initial-interval-seconds: 1
      max-interval-seconds: 100
      backoff-coefficient: 2.0
      max-attempts: 3
```
