package com.temporal.initiations.messages.api;

import jakarta.validation.constraints.NotBlank;

/**
 * Input for the File workflow.
 *
 * This record contains the minimal information needed to initiate file processing
 * via the File workflow.
 *
 * @param fileId File identifier from the request path parameter
 * @param submitterId Submitter identifier from X-Submitter-Id HTTP header
 */
public record FileWorkflowInput(
    @NotBlank(message = "File ID cannot be blank")
    String fileId,

    @NotBlank(message = "Submitter ID cannot be blank")
    String submitterId
) {
    /**
     * Compact constructor to normalize whitespace.
     */
    public FileWorkflowInput {
        fileId = fileId.trim();
        submitterId = submitterId.trim();
    }
}
