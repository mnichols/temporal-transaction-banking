package com.temporal.initiations.workflows.files;

import com.temporal.initiations.messages.domain.workflows.*;
import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface PersistenceActivities {
    PersistFileResponse persistFile(PersistFileRequest cmd);
}
