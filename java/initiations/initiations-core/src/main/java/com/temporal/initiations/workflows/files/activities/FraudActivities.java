package com.temporal.initiations.workflows.files.activities;

import com.temporal.initiations.messages.domain.workflows.CompleteFraudCheckRequest;
import com.temporal.initiations.messages.domain.workflows.CompleteFraudCheckResponse;
import com.temporal.initiations.messages.domain.workflows.StartFraudCheckRequest;
import com.temporal.initiations.messages.domain.workflows.StartFraudCheckResponse;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface FraudActivities {
    @ActivityMethod
    StartFraudCheckResponse startFraudCheck(StartFraudCheckRequest cmd);

    @ActivityMethod
    CompleteFraudCheckResponse completeFraudCheck(CompleteFraudCheckRequest cmd);
}
