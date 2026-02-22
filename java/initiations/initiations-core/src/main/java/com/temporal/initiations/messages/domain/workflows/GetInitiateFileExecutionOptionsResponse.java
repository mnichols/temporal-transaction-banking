package com.temporal.initiations.messages.domain.workflows;

public class GetInitiateFileExecutionOptionsResponse {
    private InitiateFileRequestExecutionOptions options;

    public InitiateFileRequestExecutionOptions getOptions() {
        return options;
    }

    public void setOptions(InitiateFileRequestExecutionOptions options) {
        this.options = options;
    }
}
