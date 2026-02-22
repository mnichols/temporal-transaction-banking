package com.temporal.initiations.messages.domain.workflows;

public class VerifyEntitlementsRequest {
    private String senderId;

    public VerifyEntitlementsRequest(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }
}
