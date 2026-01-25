# temporal-file-processing
Patterns for batch creation, transaction splitting, concurrent processing, etc

## Getting Started

- **[PAIN-113 Generator](files/USAGE.md)** - Generate test PAIN.001.001.03 payment XML files

## Prequisites

* Temporal Namespace named `initiation`

## Scope & Goals

This project is highly scoped to the following outcome:

> Everything required to turn intent into authorized, risk-cleared payment obligations, ready for execution.

* The Java 21 + SpringBoot implementation is found in the `java` directory
* Inside the java project, there is one module named `initiation`.
* That module contains three submodules:
  * `api` for Spring boot REST API controllers (deployable)
  * `core` for domain business logic, temporal workflows and reusable things like clients and so on. (non-deployable)
  * `workers` for Temporal Workers services (deployable)
* This will demonstrate the use of Temporal for batch processing of payment files. 
* The outputs for a completed file be N Batches created based on like criteria which will be used to group payments for routing downstream.

## Implementation 

There is only one Temporal Namespace used for this project: `initiation`.

### `core` module

has the following packages

**messages**

This is the IDL used for the API and Workflows (including activities, signals etc).

**workflows**

The Workflows, Activities, and other Temporal orchestration logic.

#### Workflow: File

This Temporal Workflow that will receive a `InitiateFileRequest`.
The steps to process the file are:

1. Perform entitlement checks on the request to process the file
2. Split the file according to the batch rules criteria.
   1. Transform the file from PAIN-113 to PAIN-116 format 
   2. Persist the transformed file records to the database
      1. For each batch of payments
         1. The criteria used for the batch is hashed for a `BatchKey` (string)
         2. UpdateWithStart the `Batch` workflow with the WorkflowID as `BatchKey`
3. Send an `ack` notification
4. The Workflow will not be completed, but will await the `approve` signal.

#### Workflow: Batch

This Temporal Workflow will receive a `InitiateBatchRequest` but will be suspended with a `wait` condition for the `approve` signal to be received.

The two steps this will support eventually are:

1. Fraud check on the batch
2. Transmit (route) the batch to the appropriate downstream system


### `api` module

Has a REST API controller with endpoints for:

`PUT /api/v1/files/{file_id}` where `file_id` is the ID of the file to be processed.

It returns a 202 Accepted for kicking off the File with a `ProcessFileRequest`.

This is a Spring application so use Temporal's Springboot starter and configure `start_workers` to be **false** since we only need a client.

### `workers` module

Is also a Spring application using Temporal's springboot starter, but this has workers on the task queue named `initiation`.
