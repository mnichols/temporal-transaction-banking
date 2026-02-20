package com.temporal.initiations.messages.domain.workflows;

import java.time.Instant;
import java.util.Objects;

public class InitiateBatchRequest {
    private Instant timestamp;
    private FileInfo fileInfo;
}
