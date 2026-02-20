package com.temporal.initiations.workflows.files;

import com.temporal.initiations.messages.domain.workflows.SendLevel1Request;
import com.temporal.initiations.messages.domain.workflows.SendLevel1Response;

public interface PaymentStatusReportActivities {
    SendLevel1Response sendLevel1(SendLevel1Request cmd);

}
