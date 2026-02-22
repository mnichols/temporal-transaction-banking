package com.temporal.initiations.messages.domain.workflows;

public class StartFraudCheckResponse {
    private String fraudCheckFilePath;

    public String getFraudCheckFilePath() {
        return fraudCheckFilePath;
    }

    public void setFraudCheckFilePath(String fraudCheckFilePath) {
        this.fraudCheckFilePath = fraudCheckFilePath;
    }
}
