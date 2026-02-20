package com.temporal.initiations.workflows.files;

import com.temporal.initiations.messages.domain.workflows.GetCustomerPreferencesRequest;
import com.temporal.initiations.messages.domain.workflows.GetCustomerPreferencesResponse;
import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface PreferencesActivities {
    GetCustomerPreferencesResponse getCustomerPreferences(GetCustomerPreferencesRequest cmd);
}
