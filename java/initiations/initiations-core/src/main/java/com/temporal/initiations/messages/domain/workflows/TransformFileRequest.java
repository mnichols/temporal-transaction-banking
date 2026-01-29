package com.temporal.initiations.messages.domain.workflows;

public record TransformFileRequest(String fileId, String filePath, String targetFormat) {
}
