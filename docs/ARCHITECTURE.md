# Architecture

## Bounded Context: Initiations

**Purpose**: Transform intent into authorized, risk-cleared payment obligations, ready for execution.

### Core Responsibilities

The `initiations` context handles:

1. **File Receipt & Validation** - Accept PAIN.001.001.03 payment initiation files
2. **Entitlement Checks** - Verify requester is authorized to process files
3. **Payment Splitting** - Divide file into batches based on payment criteria
4. **Format Transformation** - Convert from PAIN-113 to PAIN-116 format
5. **Batch Persistence** - Store batch records in database
6. **Batch Creation** - Create Batch workflow executions for each batch
7. **Approval Workflows** - Await approval signals before downstream processing
8. **Fraud Detection Setup** - Prepare fraud checks (delegated to Batch workflow)

### NOT Responsible For

- Actual fraud detection algorithms (fraud context responsibility)
- Downstream payment transmission/routing
- Payment settlement or clearing
- Compliance/regulatory reporting

## Module Structure

```
initiations/
├── initiations-core/     # Workflows, Activities, domain models
├── initiations-api/      # REST endpoints, Temporal client
└── initiations-workers/  # Worker registration, Temporal worker service
```

### initiations-core

**Non-deployable library** containing:

- **Workflows**
  - `FileWorkflow` - Orchestrates file processing
  - `BatchWorkflow` - Handles individual batch processing

- **Activities**
  - `FileValidationActivity` - Entitlement and format checks
  - `FileTransformationActivity` - PAIN-113 to PAIN-116 conversion
  - `BatchPersistenceActivity` - Store batch records
  - `NotificationActivity` - Send acknowledgments

- **Messages**
  - `InitiateFileRequest` - API request to process a file
  - `InitiateBatchRequest` - Request to process a batch
  - `PaymentRecord` - Individual payment in ISO 20022 format
  - `Batch` - Grouped payments with routing criteria

### initiations-api

**Deployable REST API service**:

- Listens on port 8080
- Provides `PUT /api/v1/files/{file_id}` endpoint
- Uses Temporal `WorkflowClient` to start `FileWorkflow` executions
- Configured with `start_workers=false` (client-only)
- Returns `202 Accepted` with workflow ID location

### initiations-workers

**Deployable worker service**:

- Listens on port 8081 (management/metrics only, no external API)
- Registers `FileWorkflow` and `BatchWorkflow` implementations
- Registers all activity implementations
- Listens on task queue: `initiations`
- Configured with `start_workers=true`

## Workflow Orchestration

### FileWorkflow Execution

```
File Submitted
    ↓
1. Validate Entitlement (Activity)
    ↓
2. Transform File (Activity)
    ↓
3. Split into Batches
    ↓
    For each Batch:
    ├─→ Persist Batch (Activity)
    └─→ Start Batch Workflow with StartWithUpdate
    ↓
4. Send Acknowledgment (Activity)
    ↓
5. AWAIT APPROVAL SIGNAL
    ↓
Workflow completes when approval signal is received
```

### BatchWorkflow Execution

```
Batch Created
    ↓
1. AWAIT APPROVAL SIGNAL
    ↓
2. Fraud Detection Check (Activity)
    ↓
3. Route/Transmit (Activity)
    ↓
Batch complete, sent downstream
```

## Temporal Configuration

- **Namespace**: `initiations`
- **Task Queue**: `initiations`
- **Timeout**: File workflow execution timeout 24 hours (configurable)

## Data Flow

```
┌─────────────────────────────────────────────────────────────────┐
│ Client                                                          │
│ PUT /api/v1/files/123                                           │
│ (PAIN.001.001.03 XML)                                           │
└─────────────┬───────────────────────────────────────────────────┘
              │
              ↓
┌─────────────────────────────────────────────────────────────────┐
│ initiations-api                                                 │
│ - Temporal Client                                               │
│ - WorkflowClient.start(FileWorkflow)                            │
│ - Returns 202 Accepted                                          │
└─────────────┬───────────────────────────────────────────────────┘
              │
              ↓
┌─────────────────────────────────────────────────────────────────┐
│ Temporal Server (Task Queue: initiations)                       │
└─────────────┬───────────────────────────────────────────────────┘
              │
              ↓
┌─────────────────────────────────────────────────────────────────┐
│ initiations-workers                                             │
│ - FileWorkflow execution                                        │
│ - Activity execution                                            │
│ - Batch workflow creation                                       │
└─────────────┬───────────────────────────────────────────────────┘
              │
              ↓
        ┌─────┴─────┐
        ↓           ↓
    Database    Batch Workflows
    (Persist    (each with approval
     batches)    signal handler)
        │           │
        └─────┬─────┘
              ↓
        Awaiting Approval
```

## Key Design Decisions

### 1. Signal-Based Approval

File and Batch workflows use **signals** for approval rather than synchronous blocking:
- Allows long-running workflows without holding API connections
- Supports human approval processes
- Enables audit trails of approval decisions

### 2. Activity-Based Orchestration

Business logic is extracted into activities:
- Enables testing and versioning
- Supports retry policies
- Allows activity-level observability

### 3. StartWithUpdate for Batch Creation

File workflow uses `WorkflowClient.startWithUpdate()` when creating batch workflows:
- Ensures batch is registered before file workflow continues
- Provides batch creation atomicity
- Batch can immediately process approved batches

### 4. Separate API and Worker Services

- API service is lightweight, scales independently
- Workers are resource-intensive, separate deployment
- Supports multiple worker instances for parallelism

## Integration Points

### Input Integration

**Source**: Payment initiation systems, API clients
**Format**: PAIN.001.001.03 XML (ISO 20022)
**Protocol**: HTTP PUT

### Output Integration

**Destination**: Downstream payment processors (fraud, routing, settlement)
**Format**: PAIN.116.001.03 XML (ISO 20022)
**Protocol**: Events/Workflows (to be implemented in separate contexts)

## Future Enhancements

- Multi-currency batch splitting
- Enhanced fraud scoring integration
- Batch modification support (before approval)
- File retry logic and error handling
- Webhook notifications for approvals
- Batch execution history and audit log
