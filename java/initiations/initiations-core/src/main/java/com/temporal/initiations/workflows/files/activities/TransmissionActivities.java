package com.temporal.initiations.workflows.files.activities;

import com.temporal.initiations.messages.domain.workflows.TransmitBatchRequest;
import com.temporal.initiations.messages.domain.workflows.TransmitBatchResponse;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface TransmissionActivities {
    @ActivityMethod
    TransmitBatchResponse transmitBatch(TransmitBatchRequest cmd);
}
