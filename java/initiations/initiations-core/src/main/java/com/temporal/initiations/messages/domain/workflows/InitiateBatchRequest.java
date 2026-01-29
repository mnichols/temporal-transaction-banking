package com.temporal.initiations.messages.domain.workflows;

public record InitiateBatchRequest(String batchKey, String fileId, String filePath) {
}
