
package com.example.secretGuard.history;

import com.example.secretGuard.scanner.SecretScanner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.example.secretGuard.scanner.SecretFingerprint;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GitHistoryScanServiceTest {

    @Mock
    private GitHistoryScanner gitHistoryScanner;

    @Mock
    private SecretScanner secretScanner;

    @InjectMocks
    private GitHistoryScanService gitHistoryScanService;

    @Test
    void shouldScanAddedLinesAndPreserveCommitInformation()
            throws Exception {

        Path repositoryPath = Path.of("test-repository");

        String commitHash = "abc123";

        Instant committedAt =
                Instant.parse("2026-11-05T10:00:00Z");

        GitHistoryScanner.CommitInfo commit =
                new GitHistoryScanner.CommitInfo(
                        commitHash,
                        "Add configuration",
                        "Test Author",
                        committedAt
                );

        GitHistoryScanner.AddedLine addedLine =
                new GitHistoryScanner.AddedLine(
                        "config.properties",
                        2,
                        "api.token=ghp_ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"
                );

        SecretScanner.SecretFinding finding =
                new SecretScanner.SecretFinding(
                        "GITHUB_TOKEN",
                        "ghp_********7890",
                        "config.properties",
                        2,
                        "Matches GitHub token format.",
                        "HIGH",
                        85,
                        "HIGH",
                        "HIGH",
                        new SecretFingerprint().generate("test-token")
                );

        when(gitHistoryScanner.getCommitHistory(repositoryPath))
                .thenReturn(List.of(commit));

        when(gitHistoryScanner.getAddedLines(
                repositoryPath, commitHash))
                .thenReturn(List.of(addedLine));

        when(secretScanner.scanLines(anyList()))
                .thenReturn(List.of(finding));

        List<GitHistoryScanService.HistoricalFinding> results =
                gitHistoryScanService.scanHistory(repositoryPath);

        assertEquals(1, results.size());

        GitHistoryScanService.HistoricalFinding result =
                results.get(0);

        assertEquals(commitHash, result.commitHash());
        assertEquals("Add configuration", result.commitMessage());
        assertEquals(committedAt, result.committedAt());

        assertEquals("GITHUB_TOKEN", result.finding().secretType());
        assertEquals("config.properties", result.finding().filePath());
        assertEquals(2, result.finding().lineNumber());

        verify(gitHistoryScanner).getAddedLines(
                repositoryPath, commitHash);

        verify(secretScanner).scanLines(anyList());
    }
}
