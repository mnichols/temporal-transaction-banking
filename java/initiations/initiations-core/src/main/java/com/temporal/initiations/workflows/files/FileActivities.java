package com.temporal.initiations.workflows.files;

import com.temporal.initiations.messages.domain.workflows.FileCheckRequest;
import com.temporal.initiations.messages.domain.workflows.FileCheckResponse;
import com.temporal.initiations.messages.domain.workflows.PersistTransformedFileRequest;
import com.temporal.initiations.messages.domain.workflows.PersistTransformedFileResponse;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface FileActivities {
    @ActivityMethod
    PersistTransformedFileResponse persistTransformedFile(PersistTransformedFileRequest cmd);

    @ActivityMethod
    FileCheckResponse checkFile(FileCheckRequest cmd);
}
