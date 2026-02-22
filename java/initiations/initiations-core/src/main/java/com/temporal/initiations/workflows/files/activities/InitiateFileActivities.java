package com.temporal.initiations.workflows.files.activities;

import com.temporal.initiations.messages.domain.workflows.GetInitiateFileExecutionOptionsRequest;
import com.temporal.initiations.messages.domain.workflows.GetInitiateFileExecutionOptionsResponse;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface InitiateFileActivities {
    @ActivityMethod
    GetInitiateFileExecutionOptionsResponse getOptions(GetInitiateFileExecutionOptionsRequest request);
}
