package com.temporal.initiations.messages.domain.workflows;

import java.util.List;

public class PersistTransformedFileResponse {
    private List<String> batchIds = new java.util.ArrayList<>();

    public List<String> getBatchIds() {
        return batchIds;
    }

    public void setBatchIds(List<String> batchIds) {
        this.batchIds = batchIds;
    }
}
