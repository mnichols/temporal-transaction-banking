package com.temporal.initiations.workflows.files.activities;

import com.temporal.initiations.messages.domain.workflows.VerifyEntitlementsRequest;
import com.temporal.initiations.messages.domain.workflows.VerifyEntitlementsResponse;
import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface EntitlementActivities {
    VerifyEntitlementsResponse verifyEntitlements(VerifyEntitlementsRequest cmd);

}
