package com.temporal.initiations.workflows.files;

import com.temporal.initiations.messages.domain.workflows.SendAckRequest;
import com.temporal.initiations.messages.domain.workflows.SendAckResponse;

public interface NotificationActivities {
    SendAckResponse sendAck(SendAckRequest cmd);

}
