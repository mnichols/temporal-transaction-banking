package com.temporal.initiations.workflows.files;

import com.temporal.initiations.messages.domain.workflows.*;
import org.springframework.stereotype.Component;

@Component( "file-initiation-activities")
public class ActivitiesImpl implements BatchActivities,
        EntitlementActivities,
        FileActivities,
        PaymentStatusReportActivities,
        PreferencesActivities
{
    @Override
    public BatchFileResponse batchFile(BatchFileRequest cmd) {

        return null;
    }

    @Override
    public ApproveBatchesResponse approveBatches(ApproveBatchesRequest cmd) {
        return null;
    }

    @Override
    public VerifyEntitlementsResponse verifyEntitlements(VerifyEntitlementsRequest cmd) {
        return null;
    }

    @Override
    public PersistTransformedFileResponse persistTransformedFile(PersistTransformedFileRequest cmd) {
        return null;
    }

    @Override
    public SendLevel1Response sendLevel1(SendLevel1Request cmd) {
        return null;
    }

    @Override
    public GetCustomerPreferencesResponse getCustomerPreferences(GetCustomerPreferencesRequest cmd) {
        return null;
    }
}
