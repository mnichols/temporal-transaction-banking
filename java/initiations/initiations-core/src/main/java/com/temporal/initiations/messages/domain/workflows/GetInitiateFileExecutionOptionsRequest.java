package com.temporal.initiations.messages.domain.workflows;

public class GetInitiateFileExecutionOptionsRequest {
    private InitiateFileRequestExecutionOptions options;

    public GetInitiateFileExecutionOptionsRequest() {
    }

    public GetInitiateFileExecutionOptionsRequest(InitiateFileRequestExecutionOptions executionOptions) {
        this.options = executionOptions;
    }

    public InitiateFileRequestExecutionOptions getOptions() {
        return options;
    }

    public void setOptions(InitiateFileRequestExecutionOptions options) {
        this.options = options;
    }
}
