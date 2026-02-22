package com.temporal.initiations.workflows.files.activities;

import com.temporal.initiations.messages.domain.workflows.ApproveBatchesRequest;
import com.temporal.initiations.messages.domain.workflows.ApproveBatchesResponse;
import com.temporal.initiations.messages.domain.workflows.BatchFileRequest;
import com.temporal.initiations.messages.domain.workflows.BatchFileResponse;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface ProcessingActivities {
    @ActivityMethod
    BatchFileResponse batchFile(BatchFileRequest cmd);

    @ActivityMethod
    ApproveBatchesResponse approveBatches(ApproveBatchesRequest cmd);
}
