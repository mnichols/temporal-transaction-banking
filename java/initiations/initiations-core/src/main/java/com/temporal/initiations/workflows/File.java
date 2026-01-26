package com.temporal.initiations.workflows;

import com.temporal.initiations.messages.api.FileWorkflowInput;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Workflow interface for processing payment files.
 *
 * This workflow handles the asynchronous processing of payment files.
 * It orchestrates validation, transformation, and persistence of payment data.
 *
 * The workflow can be signaled to approve the file after processing.
 */
@WorkflowInterface
public interface File {

    /**
     * Main workflow method for processing a payment file.
     *
     * @param input The file workflow input containing file ID and submitter information
     */
    @WorkflowMethod
    void processFile(FileWorkflowInput input);

    /**
     * Signal method to approve the processed file.
     *
     * Can be called while the workflow is waiting for approval.
     */
    @SignalMethod
    void approve();
}
