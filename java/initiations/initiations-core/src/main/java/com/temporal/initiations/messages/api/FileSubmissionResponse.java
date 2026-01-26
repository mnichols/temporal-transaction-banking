package com.temporal.initiations.messages.api;

/**
 * Response to a successful file submission.
 *
 * This record is returned with HTTP 202 Accepted when a file is successfully
 * submitted to the File workflow for asynchronous processing.
 *
 * @param workflowId The Temporal workflow ID assigned to this file processing execution
 * @param fileId Echo of the file identifier from the request
 * @param message Status message describing the submission result
 */
public record FileSubmissionResponse(
    String workflowId,
    String fileId,
    String message
) {
}
