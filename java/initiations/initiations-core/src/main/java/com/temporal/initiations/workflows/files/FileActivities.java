package com.temporal.initiations.workflows.files;

import com.temporal.initiations.messages.domain.workflows.*;
import com.temporal.initiations.messages.domain.workflows.VerifyEntitlementsResponse;
import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface FileActivities {
    PersistFileResponse persistFile(PersistFileRequest cmd);
}
