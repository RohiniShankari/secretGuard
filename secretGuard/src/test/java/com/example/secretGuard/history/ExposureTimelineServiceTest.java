
package com.example.secretGuard.history;

import com.example.secretGuard.scanner.SecretFingerprint;
import com.example.secretGuard.scanner.SecretScanner;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExposureTimelineServiceTest {

    private final SecretFingerprint fingerprint =
            new SecretFingerprint();

    @Test
    void shouldIdentifyIntroducedExposureEvent() {

        Instant committedAt =
                Instant.parse("2026-11-05T10:00:00Z");

        SecretScanner.SecretFinding finding =
                createFinding("test-token");

        GitHistoryScanService.HistoricalFinding historicalFinding =
                new GitHistoryScanService.HistoricalFinding(
                        "abc123",
                        "Add API token",
                        committedAt,
                        finding
                );

        ExposureTimelineService service =
                new ExposureTimelineService();

        List<ExposureEvent> events =
                service.identifyIntroducedEvents(
                        List.of(historicalFinding)
                );

        assertEquals(1, events.size());

        ExposureEvent event = events.get(0);

        assertEquals("INTRODUCED", event.eventType());
        assertEquals("abc123", event.commitHash());
        assertEquals("Add API token", event.commitMessage());
        assertEquals(committedAt, event.occurredAt());
    }

    @Test
    void shouldIdentifySecretRemovedFromCurrentSnapshot() {

        Instant committedAt =
                Instant.parse("2026-11-05T10:00:00Z");

        SecretScanner.SecretFinding historicalFinding =
                createFinding("test-token");

        GitHistoryScanService.HistoricalFinding historical =
                new GitHistoryScanService.HistoricalFinding(
                        "abc123",
                        "Add API token",
                        committedAt,
                        historicalFinding
                );

        ExposureTimelineService service =
                new ExposureTimelineService();

        List<ExposureEvent> events =
                service.identifyRemovedEvents(
                        List.of(historical),
                        List.of()
                );

        assertEquals(1, events.size());

        ExposureEvent event = events.get(0);

        assertEquals("REMOVED_FROM_CURRENT", event.eventType());
        assertEquals("abc123", event.commitHash());
        assertEquals(committedAt, event.occurredAt());
    }

    @Test
    void shouldNotMarkSecretAsRemovedWhenStillPresent() {

        SecretScanner.SecretFinding historicalFinding =
                createFinding("test-token");

        GitHistoryScanService.HistoricalFinding historical =
                new GitHistoryScanService.HistoricalFinding(
                        "abc123",
                        "Add API token",
                        Instant.now(),
                        historicalFinding
                );

        SecretScanner.SecretFinding currentFinding =
                createFinding("test-token");

        ExposureTimelineService service =
                new ExposureTimelineService();

        List<ExposureEvent> events =
                service.identifyRemovedEvents(
                        List.of(historical),
                        List.of(currentFinding)
                );

        assertTrue(events.isEmpty());
    }

@Test
void shouldIdentifySecretStillPresentInCurrentSnapshot() {

    Instant committedAt =
            Instant.parse("2026-11-05T10:00:00Z");

    SecretScanner.SecretFinding historicalFinding =
            createFinding("test-token");

    GitHistoryScanService.HistoricalFinding historical =
            new GitHistoryScanService.HistoricalFinding(
                    "abc123",
                    "Add API token",
                    committedAt,
                    historicalFinding
            );

    SecretScanner.SecretFinding currentFinding =
            createFinding("test-token");

    ExposureTimelineService service =
            new ExposureTimelineService();

    List<ExposureEvent> events =
            service.identifyStillPresentEvents(
                    List.of(historical),
                    List.of(currentFinding)
            );

    assertEquals(1, events.size());

    ExposureEvent event = events.get(0);

    assertEquals("STILL_PRESENT", event.eventType());
    assertEquals("abc123", event.commitHash());
    assertEquals(committedAt, event.occurredAt());
}


    private SecretScanner.SecretFinding createFinding(String secret) {

        return new SecretScanner.SecretFinding(
                "GITHUB_TOKEN",
                "ghp_********7890",
                "config.properties",
                2,
                "Matches GitHub token format.",
                "HIGH",
                85,
                "HIGH",
                "HIGH",
                fingerprint.generate(secret)
        );
    }
}
