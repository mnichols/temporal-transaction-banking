package com.temporal.initiations.workflows.files;

import com.temporal.initiations.messages.domain.workflows.TransformFileRequest;
import com.temporal.initiations.messages.domain.workflows.TransformFileResponse;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface FileActivities {
    @ActivityMethod
    TransformFileResponse transformFile(TransformFileRequest cmd);
}
