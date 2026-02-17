package com.temporal.initiations.workflows.files;

import com.temporal.initiations.messages.domain.workflows.*;
import org.springframework.stereotype.Component;

@Component( "file-initiation-activities")
public class ActivitiesImpl implements BatchActivities, EntitlementActivities, FileActivities, NotificationActivities, PersistenceActivities{
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
    public TransformFileResponse transformFile(TransformFileRequest cmd) {
        return null;
    }

    @Override
    public SendAckResponse sendAck(SendAckRequest cmd) {
        return null;
    }

    @Override
    public PersistFileResponse persistFile(PersistFileRequest cmd) {
        return null;
    }
}
