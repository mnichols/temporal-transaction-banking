package com.temporal.initiations.workflows;

import com.temporal.initiations.messages.api.InitiateFileRequest;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Workflow interface for processing payment files.
 *
 * This workflow handles the asynchronous processing of PAIN.001.001.03 XML
 * payment files submitted via the REST API. It orchestrates validation,
 * transformation, and persistence of payment data.
 *
 * The workflow can be signaled to approve the file after processing.
 */
@WorkflowInterface
public interface File {

    /**
     * Main workflow method for processing a payment file.
     *
     * Receives an InitiateFileRequest containing the file ID, XML content,
     * and submitter information. Orchestrates the processing pipeline.
     *
     * @param request The file initiation request with XML content and metadata
     */
    @WorkflowMethod
    void processFile(InitiateFileRequest request);

    /**
     * Signal method to approve the processed file.
     *
     * Can be called while the workflow is waiting for approval.
     */
    @SignalMethod
    void approve();
}
