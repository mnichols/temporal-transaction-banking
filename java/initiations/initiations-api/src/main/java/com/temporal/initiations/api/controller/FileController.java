package com.temporal.initiations.api.controller;

import com.temporal.initiations.messages.api.FileSubmissionResponse;
import com.temporal.initiations.messages.api.FileWorkflowInput;
import com.temporal.initiations.workflows.File;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

/**
 * REST controller for file workflow endpoints.
 *
 * Handles PUT requests to start File workflow executions for payment file processing.
 */
@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    private final WorkflowClient workflowClient;

    /**
     * Constructor injection of WorkflowClient.
     *
     * @param workflowClient Temporal WorkflowClient for starting workflows
     */
    public FileController(WorkflowClient workflowClient) {
        this.workflowClient = workflowClient;
    }

    /**
     * Initiates a File workflow for processing.
     *
     * Starts a File workflow with the given file ID and submitter information.
     * The workflow execution happens asynchronously.
     *
     * @param fileId File identifier from path parameter (required)
     * @param submitterId Submitter identifier from X-Submitter-Id header (required)
     * @return 202 Accepted with Location header and FileSubmissionResponse
     */
    @PutMapping("/{file_id}")
    public ResponseEntity<FileSubmissionResponse> submitFile(
        @PathVariable("file_id") String fileId,
        @RequestHeader("X-Submitter-Id") String submitterId
    ) {
        // Start workflow and get workflow ID
        String workflowId = startFileWorkflow(fileId, submitterId);

        // Build response
        FileSubmissionResponse response = new FileSubmissionResponse(
            workflowId,
            fileId,
            "Workflow initiated for processing"
        );

        // Build Location header pointing to workflow status endpoint
        URI location = URI.create("/api/v1/files/" + fileId + "/status/" + workflowId);

        return ResponseEntity
            .accepted()
            .location(location)
            .body(response);
    }

    /**
     * Starts a File workflow execution.
     *
     * Generates a unique workflow ID combining the file ID and a UUID.
     * Submits the workflow to the initiations task queue for execution.
     *
     * @param fileId The file identifier
     * @param submitterId The submitter identifier
     * @return The generated workflow ID
     */
    private String startFileWorkflow(String fileId, String submitterId) {
        // Generate unique workflow ID
        String workflowId = fileId + "-" + UUID.randomUUID();

        // Configure workflow options
        WorkflowOptions workflowOptions = WorkflowOptions.newBuilder()
            .setWorkflowId(workflowId)
            .setTaskQueue("initiations")
            .build();

        // Create workflow input POJO
        FileWorkflowInput input = new FileWorkflowInput(fileId, submitterId);

        // Create typed workflow stub and start workflow
        File workflow = workflowClient.newWorkflowStub(File.class, workflowOptions);
        WorkflowClient.start(workflow::processFile, input);

        return workflowId;
    }
}
