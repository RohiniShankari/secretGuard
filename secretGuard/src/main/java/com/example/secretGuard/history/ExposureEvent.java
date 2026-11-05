
package com.example.secretGuard.history;

import java.time.Instant;

public record ExposureEvent(
        String eventType,
        String commitHash,
        String commitMessage,
        Instant occurredAt
) {}
