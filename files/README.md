# Payment Initiation – File-Based Integration (ISO 20022)

## Overview

This module implements **file-based payment initiation** using the ISO 20022
XML standard. It accepts an internal payment instruction model and produces
standards-compliant **PAIN messages** for downstream processing.

The overall processing flow is:

```
Internal Payment Model
        ↓
PAIN.001 (Customer Credit Transfer Initiation)
        ↓
Downstream Processing / Validation
        ↓
PAIN.116 (Customer Credit Transfer Processing / Status)
```

This document describes:
- Supported ISO 20022 message types and versions
- PAIN.001 file structure and required fields
- A schema-valid PAIN.001 XML example
- PAIN.116 usage and versioning expectations

---

## Supported Message Standards

| Message | Purpose |
|-------|---------|
| `pain.001` | Customer Credit Transfer Initiation |
| `pain.116` | Customer Credit Transfer Processing / Status |

### Versions

| Message | Version |
|-------|---------|
| PAIN.001 | `pain.001.001.03` |
| PAIN.116 | `pain.116.001.03` |

> **Note:** `pain.116.001.03` is the most commonly used PAIN.116 version in
> large U.S. clearing-bank file-processing environments.

---

## PAIN.001 Message Structure

A PAIN.001 document consists of three hierarchical levels:

1. **Group Header (`GrpHdr`)** – file-level metadata
2. **Payment Information (`PmtInf`)** – debtor and batch-level data
3. **Credit Transfer Transactions (`CdtTrfTxInf`)** – individual payments

```
Document
└── CstmrCdtTrfInitn
    ├── GrpHdr
    └── PmtInf
        └── CdtTrfTxInf (1..n)
```

---

## Field Specifications

### Group Header (`GrpHdr`)

| Field | Required | Description |
|------|----------|------------|
| `MsgId` | Yes | Unique identifier for the file |
| `CreDtTm` | Yes | File creation timestamp |
| `NbOfTxs` | Yes | Total number of transactions |
| `CtrlSum` | Recommended | Sum of all transaction amounts |
| `InitgPty/Nm` | Yes | Initiating party name |
| `InitgPty/Id` | Optional | Initiating party identifier |

---

### Payment Information (`PmtInf`)

| Field | Required | Description |
|------|----------|------------|
| `PmtInfId` | Yes | Payment batch identifier |
| `PmtMtd` | Yes | Payment method (`TRF`) |
| `BtchBookg` | Optional | Batch booking indicator |
| `NbOfTxs` | Recommended | Number of transactions in batch |
| `CtrlSum` | Recommended | Batch control sum |
| `ReqdExctnDt` | Yes | Requested execution date |
| `Dbtr/Nm` | Yes | Debtor name |
| `DbtrAcct/Id` | Yes | Debtor account identifier |
| `DbtrAgt` | Optional | Debtor financial institution |

---

### Credit Transfer Transaction (`CdtTrfTxInf`)

| Field | Required | Description |
|------|----------|------------|
| `PmtId/EndToEndId` | Yes | Unique transaction reference |
| `Amt/InstdAmt` | Yes | Amount and currency |
| `Cdtr/Nm` | Yes | Creditor name |
| `CdtrAcct/Id` | Yes | Creditor account identifier |
| `CdtrAgt` | Optional | Creditor financial institution |
| `RmtInf/Ustrd` | Optional | Remittance information |
| `ChrgBr` | Optional | Charge bearer |

---

## Sample PAIN.001 XML (Generic)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Document xmlns="urn:iso:std:iso:20022:tech:xsd:pain.001.001.03">
  <CstmrCdtTrfInitn>

    <GrpHdr>
      <MsgId>MSG-20260125-0001</MsgId>
      <CreDtTm>2026-01-25T15:00:00</CreDtTm>
      <NbOfTxs>1</NbOfTxs>
      <CtrlSum>123.45</CtrlSum>
      <InitgPty>
        <Nm>Example Initiating Party</Nm>
        <Id>
          <OrgId>
            <Othr>
              <Id>INITIATOR-001</Id>
            </Othr>
          </OrgId>
        </Id>
      </InitgPty>
    </GrpHdr>

    <PmtInf>
      <PmtInfId>PMTINF-001</PmtInfId>
      <PmtMtd>TRF</PmtMtd>
      <NbOfTxs>1</NbOfTxs>
      <CtrlSum>123.45</CtrlSum>
      <ReqdExctnDt>2026-01-26</ReqdExctnDt>

      <Dbtr>
        <Nm>Example Debtor</Nm>
      </Dbtr>
      <DbtrAcct>
        <Id>
          <Othr>
            <Id>DEBTOR-ACCOUNT-001</Id>
          </Othr>
        </Id>
      </DbtrAcct>

      <CdtTrfTxInf>
        <PmtId>
          <EndToEndId>E2E-0001</EndToEndId>
        </PmtId>

        <Amt>
          <InstdAmt Ccy="USD">123.45</InstdAmt>
        </Amt>

        <Cdtr>
          <Nm>Example Creditor</Nm>
        </Cdtr>
        <CdtrAcct>
          <Id>
            <Othr>
              <Id>CREDITOR-ACCOUNT-001</Id>
            </Othr>
          </Id>
        </CdtrAcct>

        <RmtInf>
          <Ustrd>Invoice 12345</Ustrd>
        </RmtInf>

        <ChrgBr>SLEV</ChrgBr>
      </CdtTrfTxInf>
    </PmtInf>

  </CstmrCdtTrfInitn>
</Document>
```

---

## PAIN.116 Processing

After submission and downstream processing, **PAIN.116** messages are produced
to represent validation, processing, acceptance, rejection, or settlement
status.

This module does not originate PAIN.116 messages but is designed to integrate
with systems that consume or emit them.

### Common Version

```
pain.116.001.03
```

---

## Validation Notes

- XML must validate against the official ISO 20022 XSD.
- `CtrlSum` and `NbOfTxs` must match actual transaction content.
- `EndToEndId` must be unique per transaction.
- Account and agent identifiers are interpreted by downstream routing logic.
- This module performs structural and semantic validation only.

---

## Summary

- Produces bank-neutral PAIN.001 files
- Supports transformation into PAIN.116 for processing
- Uses ISO 20022 standard structures
- Avoids embedding bank- or channel-specific logic

---

## Test File Generation

The `generate_pain_113.py` script generates PAIN.001.001.03 test files with configurable record counts. See [USAGE.md](USAGE.md) for detailed instructions.

Quick start:
```bash
cd files
python3 generate_pain_113.py        # Generate 100 records
python3 generate_pain_113.py 5000   # Generate 5,000 records
python3 generate_pain_113.py 25000  # Generate 25,000 records
```

Generated files are stored in `generated/` which is gitignored.

---

## References

- ISO 20022 Message Definitions
- PAIN.001 / PAIN.116 XML Schemas
