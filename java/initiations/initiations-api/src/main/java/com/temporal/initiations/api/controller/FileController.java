package com.temporal.initiations.api.controller;

import com.temporal.initiations.messages.api.InitiateFileRequest;
import com.temporal.initiations.messages.api.FileSubmissionResponse;
import com.temporal.initiations.api.validation.PainXmlValidator;
import com.temporal.initiations.workflows.File;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

/**
 * REST controller for file submission endpoints.
 *
 * Handles PUT requests to submit PAIN.001.001.03 XML payment files
 * for asynchronous processing via Temporal workflows.
 */
@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    private final WorkflowClient workflowClient;
    private final PainXmlValidator xmlValidator;

    /**
     * Constructor injection of dependencies.
     *
     * @param workflowClient Temporal WorkflowClient for starting workflows
     * @param xmlValidator Validator for PAIN XML content
     */
    public FileController(WorkflowClient workflowClient, PainXmlValidator xmlValidator) {
        this.workflowClient = workflowClient;
        this.xmlValidator = xmlValidator;
    }

    /**
     * Submits a payment file for processing.
     *
     * Accepts raw PAIN.001.001.03 XML content in the request body, validates it,
     * and submits it to the File workflow for asynchronous processing.
     *
     * @param fileId File identifier from path parameter (required)
     * @param xmlContent Raw XML content from request body (required)
     * @param submitterId Submitter identifier from X-Submitter-Id header (required)
     * @return 202 Accepted with Location header and FileSubmissionResponse
     * @throws com.temporal.initiations.api.exception.XmlParsingException if XML is invalid
     * @throws com.temporal.initiations.api.exception.ValidationException if validation fails
     */
    @PutMapping("/{file_id}")
    public ResponseEntity<FileSubmissionResponse> submitFile(
        @PathVariable("file_id") String fileId,
        @RequestBody String xmlContent,
        @RequestHeader("X-Submitter-Id") String submitterId
    ) {
        // Validate XML content
        xmlValidator.validate(xmlContent);

        // Create request DTO
        InitiateFileRequest request = new InitiateFileRequest(fileId, xmlContent, submitterId);

        // Start workflow and get workflow ID
        String workflowId = startFileWorkflow(request);

        // Build response
        FileSubmissionResponse response = new FileSubmissionResponse(
            workflowId,
            fileId,
            "File accepted for processing"
        );

        // Build Location header pointing to workflow status endpoint
        URI location = URI.create("/api/v1/files/" + fileId + "/status/" + workflowId);

        return ResponseEntity
            .accepted()
            .location(location)
            .body(response);
    }

    /**
     * Starts a File workflow execution for the given request.
     *
     * Generates a unique workflow ID combining the file ID and a UUID.
     * Submits the workflow to the initiations task queue for execution.
     *
     * @param request The InitiateFileRequest with file details
     * @return The generated workflow ID
     */
    private String startFileWorkflow(InitiateFileRequest request) {
        // Generate unique workflow ID
        String workflowId = request.fileId() + "-" + UUID.randomUUID();

        // Configure workflow options
        WorkflowOptions workflowOptions = WorkflowOptions.newBuilder()
            .setWorkflowId(workflowId)
            .setTaskQueue("initiations")
            .build();

        // Create typed workflow stub and start workflow
        File workflow = workflowClient.newWorkflowStub(File.class, workflowOptions);
        WorkflowClient.start(workflow::processFile, request);

        return workflowId;
    }
}
