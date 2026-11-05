
package com.example.secretGuard.history;

import com.example.secretGuard.scanner.SecretScanner;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class GitHistoryScanService {

    private final GitHistoryScanner gitHistoryScanner;
    private final SecretScanner secretScanner;

    public GitHistoryScanService(
            GitHistoryScanner gitHistoryScanner,
            SecretScanner secretScanner) {

        this.gitHistoryScanner = gitHistoryScanner;
        this.secretScanner = secretScanner;
    }

    public List<HistoricalFinding> scanHistory(
            java.nio.file.Path repositoryPath) throws IOException {

        List<HistoricalFinding> historicalFindings =
                new ArrayList<>();

        List<GitHistoryScanner.CommitInfo> commits =
                gitHistoryScanner.getCommitHistory(repositoryPath);

        for (GitHistoryScanner.CommitInfo commit : commits) {

            List<GitHistoryScanner.AddedLine> addedLines =
                    gitHistoryScanner.getAddedLines(
                            repositoryPath,
                            commit.commitHash()
                    );

            List<SecretScanner.ScanLine> scanLines =
                    addedLines.stream()
                            .map(line -> new SecretScanner.ScanLine(
                                    line.filePath(),
                                    line.lineNumber(),
                                    line.content()
                            ))
                            .toList();

            List<SecretScanner.SecretFinding> findings =
                    secretScanner.scanLines(scanLines);

            for (SecretScanner.SecretFinding finding : findings) {

                historicalFindings.add(new HistoricalFinding(
                        commit.commitHash(),
                        commit.message(),
                        commit.committedAt(),
                        finding
                ));
            }
        }

        return historicalFindings;
    }

    public record HistoricalFinding(
            String commitHash,
            String commitMessage,
            java.time.Instant committedAt,
            SecretScanner.SecretFinding finding
    ) {}
}
