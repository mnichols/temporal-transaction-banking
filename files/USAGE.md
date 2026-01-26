# PAIN-113 Generator Usage

## Overview

This document describes how to generate the `pain-113-25k.xml` test file containing 25,000 payment records in PAIN.001.001.03 format.

## Prerequisites

- Python 3.8+

## Running the Generator

From the `files/` directory, execute:

```bash
cd files

# Generate default (100 records)
python3 generate_pain_113.py

# Generate specific number of records
python3 generate_pain_113.py 25000
python3 generate_pain_113.py 5000
python3 generate_pain_113.py 1000
```

## Output

The script will generate files in `files/generated/`:

- **Default (100 records):** `files/generated/pain-113-100.xml`
- **With argument:** `files/generated/pain-113-{NUM_RECORDS}.xml` or `files/generated/pain-113-{NUM}k.xml` for thousands
- **Format:** ISO 20022 PAIN.001.001.03 XML
- **Currency:** USD

Example outputs:
- `files/generated/pain-113-100.xml` (100 records)
- `files/generated/pain-113-25k.xml` (25,000 records)
- `files/generated/pain-113-5000.xml` (5,000 records)

The script prints progress updates every 5,000 records and displays the final file size and total transaction amount when complete.

## File Structure

The generated XML includes:

- **Group Header (`GrpHdr`)** - File metadata (message ID, creation timestamp, transaction count, control sum)
- **Payment Information (`PmtInf`)** - Debtor and batch-level data with a single master debtor account
- **Credit Transfer Transactions (`CdtTrfTxInf`)** - 25,000 individual payment records, each with:
  - Unique end-to-end ID
  - Random amount (USD 100 - USD 50,000)
  - Generated creditor name and account ID
  - Payment reference

## Notes

- Each execution generates a new file with the same name, overwriting any previous version
- Transaction amounts are randomized; control sums are recalculated each run
- Account identifiers are randomly generated for testing purposes
- The execution date is set to the day following generation
- All generated files are stored in `files/generated/` which is gitignored
