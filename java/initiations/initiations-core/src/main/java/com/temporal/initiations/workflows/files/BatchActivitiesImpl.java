package com.temporal.initiations.workflows.files;

import com.temporal.initiations.messages.domain.workflows.ApproveBatchesRequest;
import com.temporal.initiations.messages.domain.workflows.ApproveBatchesResponse;
import com.temporal.initiations.messages.domain.workflows.BatchFileRequest;
import com.temporal.initiations.messages.domain.workflows.BatchFileResponse;

public class BatchActivitiesImpl implements BatchActivities {
    @Override
    public BatchFileResponse batchFile(BatchFileRequest cmd) {
        // for each item in the batchKeys kick off Batch Workflow with updateWithStart
        return null;
    }

    @Override
    public BatchKeyResponse computeKeys(BatchKeyRequest cmd) {
        return null;
    }

    @Override
    public ApproveBatchesResponse approveBatches(ApproveBatchesRequest cmd) {
        return null;
    }
}
