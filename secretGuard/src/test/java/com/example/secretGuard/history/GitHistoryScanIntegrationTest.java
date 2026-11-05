
package com.example.secretGuard.history;

import com.example.secretGuard.scanner.*;
import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;


import static org.junit.jupiter.api.Assertions.*;

class GitHistoryScanIntegrationTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldDetectSecretEvenAfterItIsRemovedFromCurrentFile()
            throws Exception {

        Path repositoryPath = tempDir.resolve("repository");
        Files.createDirectories(repositoryPath);

        String token =
                "ghp_ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";

        try (Git git = Git.init()
                .setDirectory(repositoryPath.toFile())
                .call()) {

            // First commit: add a synthetic GitHub token.
            Path configFile =
                    repositoryPath.resolve("config.properties");

            Files.writeString(
                    configFile,
                    "api.token=" + token + "\n"
            );

            git.add().addFilepattern("config.properties").call();

            git.commit()
                    .setMessage("Add API token")
                    .setAuthor("Test User", "test@example.com")
                    .call();

            // Second commit: remove the token.
            Files.writeString(
                    configFile,
                    "app.name=SecretGuard\n"
            );

            git.add().addFilepattern("config.properties").call();

            git.commit()
                    .setMessage("Remove API token")
                    .setAuthor("Test User", "test@example.com")
                    .call();
        }

        // Create the actual scanner dependencies.
        SecretPatternConfig patternConfig = new SecretPatternConfig();

        SecretScanner secretScanner = new SecretScanner(
                patternConfig,
                new RepositoryFileService(),
                new PlaceholderDetector(),
                new ContextAnalyzer(),
                new EntropyAnalyzer(),
                new ConfidenceAnalyzer(),
                new SeverityAnalyzer(),
                new RiskAnalyzer(),
                new SecretFingerprint()
        );

        GitHistoryScanner gitHistoryScanner =
                new GitHistoryScanner();

        GitHistoryScanService historyScanService =
                new GitHistoryScanService(
                        gitHistoryScanner,
                        secretScanner
                );

        // Scan the complete commit history.
        List<GitHistoryScanService.HistoricalFinding> findings =
                historyScanService.scanHistory(repositoryPath);

        // The token should still be detected in its original commit.
        assertEquals(1, findings.size());

        GitHistoryScanService.HistoricalFinding finding =
                findings.get(0);

        assertEquals("Add API token", finding.commitMessage());
        assertEquals(
                "GITHUB_TOKEN",
                finding.finding().secretType()
        );

        assertEquals(
                "config.properties",
                finding.finding().filePath()
        );

        // Ensure the original token is not exposed in the result.
        assertFalse(
                finding.finding().maskedValue().contains(token)
        );
        
     // Scan the current repository snapshot.
        List<SecretScanner.SecretFinding> currentFindings =
                secretScanner.scanFiles(repositoryPath);

        // Generate the removal event.
        ExposureTimelineService timelineService =
                new ExposureTimelineService();

        List<ExposureEvent> events =
                timelineService.identifyRemovedEvents(
                        findings,
                        currentFindings
                );

        // Verify that the historical secret was removed
        // from the current snapshot.
        assertTrue(currentFindings.isEmpty());

        assertEquals(1, events.size());

        assertEquals(
                "REMOVED_FROM_CURRENT",
                events.get(0).eventType()
        );

        assertEquals(
                finding.commitHash(),
                events.get(0).commitHash()
        );
    }
    

@Test
void shouldDetectSecretStillPresentInCurrentRepository()
        throws Exception {

    Path repositoryPath = tempDir.resolve("repository-still-present");
    Files.createDirectories(repositoryPath);

    try (Git git = Git.init()
            .setDirectory(repositoryPath.toFile())
            .call()) {

        Path configFile =
                repositoryPath.resolve("config.properties");

        Files.writeString(
                configFile,
                "api.token=ghp_ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890\n"
        );

        git.add().addFilepattern("config.properties").call();

        git.commit()
                .setMessage("Add API token")
                .setAuthor("Test User", "test@example.com")
                .call();
    }

    SecretScanner secretScanner = new SecretScanner(
            new SecretPatternConfig(),
            new RepositoryFileService(),
            new PlaceholderDetector(),
            new ContextAnalyzer(),
            new EntropyAnalyzer(),
            new ConfidenceAnalyzer(),
            new SeverityAnalyzer(),
            new RiskAnalyzer(),
            new SecretFingerprint()
    );

    GitHistoryScanner gitHistoryScanner =
            new GitHistoryScanner();

    GitHistoryScanService historyScanService =
            new GitHistoryScanService(
                    gitHistoryScanner,
                    secretScanner
            );

    List<GitHistoryScanService.HistoricalFinding> historicalFindings =
            historyScanService.scanHistory(repositoryPath);

    List<SecretScanner.SecretFinding> currentFindings =
            secretScanner.scanFiles(repositoryPath);

    ExposureTimelineService timelineService =
            new ExposureTimelineService();

    List<ExposureEvent> introducedEvents =
            timelineService.identifyIntroducedEvents(historicalFindings);

    assertEquals(1, introducedEvents.size());
    assertEquals("INTRODUCED", introducedEvents.get(0).eventType());
    assertEquals("Add API token", introducedEvents.get(0).commitMessage());
    List<ExposureEvent> events =
            timelineService.identifyStillPresentEvents(
                    historicalFindings,
                    currentFindings
            );

    assertEquals(1, events.size());

    assertEquals(
            "STILL_PRESENT",
            events.get(0).eventType()
    );

    assertEquals(
            "Secret is still present in the current snapshot.",
            events.get(0).commitMessage()
    );
}

}
