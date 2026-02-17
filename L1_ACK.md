# L1_ACK

## Goals

1. Enable a more responsive `ack` to customers who have uploaded a file while continuing processing in the background.
2. Support _batch_ processing of each file as efficiently as possible.
3. Simplify approval and notification processes for each file and its associated batches.


## Non-Goals

1. Support all file types. Presume PAIN-113 file upload.
2. Optimizing the file watcher and delivery. We presume the file has a name, and id, and a path.
3. Audit logging
4. Support for discovery of all notification channels per file. The L1 `ack` delivery will be abstracted away.

## Requirements

### Constraints

#### _Customer notifications_

* Are subject to customer notification settings
* Are sent for each of the following stage of file processing:

  | Level | What It Represents        | Meaning                                                      |
  |-------|--------------------------|--------------------------------------------------------------|
  | L1    | Technical Acknowledgment | File received & syntax/schema valid                         |
  | L2    | Business Validation      | File/batch accepted or rejected based on business rules    |
  | L3    | Clearing Status          | Accepted/rejected by external clearing system               |
  | L4    | Settlement Status        | Funds movement completed / final disposition                |

#### _Uploaded Files_

* 15+ file formats are supported. 
* We will start with only PAIN-113 -> PAIN-116 transformation.

#### _Each file_

* Has a universally unique FileID.
* Belongs to exactly one SenderID
* Only processes payments authorized by an external `entitlements` check for the SenderID
* Has a maximum transaction count of 25K
* Is subject to batch processing, regardless of transaction count or batch count.
* Is approvable, but may be auto-approved based on customer approval settings.
* Should be referenced by an `Level1_Ack` notification for the customer

#### _Each Batch_

* Has a universally unique BatchID (not to be confused with BatchKey)
* Is determined by a computed **BatchKey** (from ~10-12 parameters):
  * Some parameters for this key are derived from the transaction.
  * Some parameters are obtained by an XLS specification maintained by the customer.
* Is approvable, based on customer approval settings.
* Is subject to Fraud Detection (via GFD file interop)
* Routes transactions to a singular payment method at GPO
  * That is, it supports exactly one lane
* ALWAYS transmits to GPO its transactions in one "go"
* ALWAYS transmits to GPO in PAIN-116 XML format

#### _Each Transaction_

* Has an unique EndToEndID
* Is enriched by external metadata.
* Is stored in a database with the following schema:
  * BatchKey
  * Searchable Metadata
  * PAIN-116 XML Blob


## Open Questions

### Bottlenecks

Between the file having been placed into the stage directory and the L1 `ack` being sent to the customer,
what are the bottlenecks that are preventing a timely ack?

1. Is it during transformation?
2. Is it the entitlements check?



## Calculating the BatchKeys
1. Where in transformation do the other customer fields come from that are used to create a BatchKey?
2. Is that enrichment data (from the XLS) loaded _once_ per file or repeatedly?


# Notes

```json
# start_processing_file
{
  "file_id/batch_group_id/tran_id": "F1234567890",
  "user_id": "1234567890", // distinct from entitlements check
  "file_name": "file.xml",
}

<NbOfTxs>


```


Format of upload (host to Host): {CompanyID}_{ProfileID}_{FileID}.xml
Format of upload (UI): {CompanyID}_{FileID}.xml


1. Business rules are plain ole Java. 