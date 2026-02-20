package com.temporal.initiations.workflows.files;

import com.temporal.initiations.messages.domain.workflows.ApproveBatchesRequest;
import com.temporal.initiations.messages.domain.workflows.ApproveBatchesResponse;
import com.temporal.initiations.messages.domain.workflows.BatchFileRequest;
import com.temporal.initiations.messages.domain.workflows.BatchFileResponse;

public interface BatchActivities {
    BatchFileResponse batchFile(BatchFileRequest cmd);
    // [string]
    ApproveBatchesResponse approveBatches(ApproveBatchesRequest cmd);
}
