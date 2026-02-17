package com.temporal.initiations.workflows.files;

import com.temporal.initiations.messages.domain.workflows.ApproveFileRequest;
import com.temporal.initiations.messages.domain.workflows.InitiateFileRequest;
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
     * @param args The file workflow args containing file ID and submitter information*
     */
    @WorkflowMethod
    void execute(InitiateFileRequest args);

    /**
     * Signal method to approve the processed file.
     *
     * Can be called while the workflow is waiting for approval.
     */
    @SignalMethod
    void approve(ApproveFileRequest cmd);

}
