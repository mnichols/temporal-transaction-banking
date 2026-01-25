#!/usr/bin/env python3
"""Generate PAIN.001.001.03 XML file with configurable number of payment records."""

import xml.etree.ElementTree as ET
from datetime import datetime, timedelta
import random
import string
import sys

NAMESPACE = 'urn:iso:std:iso:20022:tech:xsd:pain.001.001.03'

# Default to 100 records if no argument provided
NUM_RECORDS = int(sys.argv[1]) if len(sys.argv) > 1 else 100
OUTPUT_FILE = f'generated/pain-113-{NUM_RECORDS // 1000}k.xml' if NUM_RECORDS >= 1000 else f'generated/pain-113-{NUM_RECORDS}.xml'

def generate_account_id():
    """Generate a realistic-looking account identifier."""
    return f"ACC{random.randint(10000000, 99999999)}"

def generate_creditor_name():
    """Generate realistic creditor names."""
    first_names = ['John', 'Jane', 'Robert', 'Mary', 'Michael', 'Patricia', 'James', 'Linda']
    last_names = ['Smith', 'Johnson', 'Williams', 'Brown', 'Jones', 'Garcia', 'Miller', 'Davis']
    companies = ['Inc.', 'LLC', 'Corp.', 'Ltd.', 'Solutions', 'Services', 'Group', 'Enterprises']

    if random.random() > 0.5:
        return f"{random.choice(first_names)} {random.choice(last_names)}"
    else:
        return f"{random.choice(first_names)} {random.choice(last_names)} {random.choice(companies)}"

def create_element(parent, tag, text=None, attribs=None):
    """Helper to create XML elements."""
    elem = ET.SubElement(parent, tag)
    if text is not None:
        elem.text = str(text)
    if attribs:
        for key, value in attribs.items():
            elem.set(key, value)
    return elem

def generate_pain_113():
    """Generate PAIN.001.001.03 XML with configurable payment records."""

    # Register namespace
    ET.register_namespace('', NAMESPACE)

    # Create root document
    doc = ET.Element('Document')
    doc.set('xmlns', NAMESPACE)

    cstmr_cdt_trf_initn = create_element(doc, 'CstmrCdtTrfInitn')

    # Calculate totals
    total_amount = 0.0
    amounts = []
    for _ in range(NUM_RECORDS):
        amt = round(random.uniform(100, 50000), 2)
        amounts.append(amt)
        total_amount += amt

    # Group Header
    grp_hdr = create_element(cstmr_cdt_trf_initn, 'GrpHdr')
    create_element(grp_hdr, 'MsgId', f'MSG-{datetime.now().strftime("%Y%m%d%H%M%S")}-001')
    create_element(grp_hdr, 'CreDtTm', datetime.now().isoformat())
    create_element(grp_hdr, 'NbOfTxs', NUM_RECORDS)
    create_element(grp_hdr, 'CtrlSum', f'{total_amount:.2f}')

    initg_pty = create_element(grp_hdr, 'InitgPty')
    create_element(initg_pty, 'Nm', 'Payment Initiator')

    # Payment Information
    pmt_inf = create_element(cstmr_cdt_trf_initn, 'PmtInf')
    create_element(pmt_inf, 'PmtInfId', f'PMTINF-{datetime.now().strftime("%Y%m%d%H%M%S")}')
    create_element(pmt_inf, 'PmtMtd', 'TRF')
    create_element(pmt_inf, 'NbOfTxs', NUM_RECORDS)
    create_element(pmt_inf, 'CtrlSum', f'{total_amount:.2f}')

    # Execution date (tomorrow)
    exec_date = (datetime.now() + timedelta(days=1)).strftime('%Y-%m-%d')
    create_element(pmt_inf, 'ReqdExctnDt', exec_date)

    # Debtor Information
    dbtr = create_element(pmt_inf, 'Dbtr')
    create_element(dbtr, 'Nm', 'Primary Debtor Account')

    dbtr_acct = create_element(pmt_inf, 'DbtrAcct')
    dbtr_acct_id = create_element(dbtr_acct, 'Id')
    dbtr_acct_othr = create_element(dbtr_acct_id, 'Othr')
    create_element(dbtr_acct_othr, 'Id', 'DEBTOR-MASTER-001')

    # Generate payment records
    print(f"Generating {NUM_RECORDS:,} payment records...")
    for i in range(1, NUM_RECORDS + 1):
        if NUM_RECORDS > 5000 and i % 5000 == 0:
            print(f"  Generated {i:,} records...")

        txn_inf = create_element(pmt_inf, 'CdtTrfTxInf')

        # Payment ID
        pmt_id = create_element(txn_inf, 'PmtId')
        create_element(pmt_id, 'EndToEndId', f'E2E-{i:08d}')

        # Amount
        amt = create_element(txn_inf, 'Amt')
        create_element(amt, 'InstdAmt', f'{amounts[i-1]:.2f}', {'Ccy': 'USD'})

        # Creditor
        cdtr = create_element(txn_inf, 'Cdtr')
        create_element(cdtr, 'Nm', generate_creditor_name())

        # Creditor Account
        cdtr_acct = create_element(txn_inf, 'CdtrAcct')
        cdtr_acct_id = create_element(cdtr_acct, 'Id')
        cdtr_acct_othr = create_element(cdtr_acct_id, 'Othr')
        create_element(cdtr_acct_othr, 'Id', generate_account_id())

        # Remittance Information
        rmtinf = create_element(txn_inf, 'RmtInf')
        create_element(rmtinf, 'Ustrd', f'Payment ref {i:08d}')

    # Write to file with pretty formatting
    tree = ET.ElementTree(doc)
    ET.indent(tree, space="  ")
    tree.write(OUTPUT_FILE, encoding='UTF-8', xml_declaration=True)

    # Print completion info
    import os
    file_size = os.path.getsize(OUTPUT_FILE)
    file_size_mb = file_size / (1024 * 1024)

    print(f"\nSuccessfully generated {OUTPUT_FILE}")
    print(f"Records: {NUM_RECORDS:,}")
    print(f"Total Amount: ${total_amount:,.2f}")
    print(f"File Size: {file_size_mb:.2f} MB")

if __name__ == '__main__':
    generate_pain_113()
