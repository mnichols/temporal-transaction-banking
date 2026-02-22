package com.temporal.initiations.messages.domain.workflows;

public class VerifyEntitlementsResponse {
    private boolean isUnauthorized;

    public boolean isUnauthorized() {
        return isUnauthorized;
    }

    public void setUnauthorized(boolean unauthorized) {
        isUnauthorized = unauthorized;
    }
}
