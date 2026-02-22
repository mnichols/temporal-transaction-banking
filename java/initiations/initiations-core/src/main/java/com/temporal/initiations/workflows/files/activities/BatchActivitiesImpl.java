package com.temporal.initiations.workflows.files.activities;

import com.temporal.initiations.messages.domain.workflows.*;
import org.springframework.stereotype.Component;

@Component("batch-processing-activities")
public class BatchActivitiesImpl implements BatchActivities, TransmissionActivities, FraudActivities {

    @Override
    public TransmitBatchResponse transmitBatch(TransmitBatchRequest cmd) {
        return null;
    }

    @Override
    public StartFraudCheckResponse startFraudCheck(StartFraudCheckRequest cmd) {
        return null;
    }

    @Override
    public CompleteFraudCheckResponse completeFraudCheck(CompleteFraudCheckRequest cmd) {
        return null;
    }
}
