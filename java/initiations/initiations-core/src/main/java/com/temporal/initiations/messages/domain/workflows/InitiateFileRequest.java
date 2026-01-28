package com.temporal.initiations.messages.domain.workflows;

import jakarta.validation.constraints.NotBlank;

/**
 * Request to initiate processing of a payment file.
 *
 * This record contains the file identifier, XML content, and submitter information
 * needed to start a File workflow execution. All fields are required and validated
 * to be non-blank.
 *
 * @param fileId File identifier from the request path parameter
 * @param submitterId Submitter identifier from X-Submitter-Id HTTP header
 */
public record InitiateFileRequest(
    @NotBlank(message = "File ID cannot be blank")
    String fileId,

    @NotBlank(message = "Submitter ID cannot be blank")
    String submitterId
) {
    /**
     * Compact constructor to normalize whitespace.
     */
    public InitiateFileRequest {
        fileId = fileId.trim();
        submitterId = submitterId.trim();
    }

}
