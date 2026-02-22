package com.temporal.initiations.workflows.files.activities;

import com.temporal.initiations.messages.domain.workflows.FileCheckRequest;
import com.temporal.initiations.messages.domain.workflows.FileCheckResponse;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface FileCheckActivities {

    @ActivityMethod
    FileCheckResponse checkFile(FileCheckRequest cmd);
}
