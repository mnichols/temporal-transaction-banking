package com.temporal.initiations.workflows.files.activities;

import com.temporal.initiations.messages.domain.workflows.FileCheckRequest;
import com.temporal.initiations.messages.domain.workflows.FileCheckResponse;
import com.temporal.initiations.messages.domain.workflows.PersistTransformedFileRequest;
import com.temporal.initiations.messages.domain.workflows.PersistTransformedFileResponse;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface TransformationActivities {
    @ActivityMethod
    PersistTransformedFileResponse persistTransformedFile(PersistTransformedFileRequest cmd);

}
