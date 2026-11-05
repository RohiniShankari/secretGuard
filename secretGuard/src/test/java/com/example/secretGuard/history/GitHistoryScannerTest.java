
package com.example.secretGuard.history;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GitHistoryScannerTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldReadCommitHistory() throws Exception {

        try (Git git = Git.init()
                .setDirectory(tempDir.toFile())
                .call()) {

            Path file = tempDir.resolve("README.md");
            Files.writeString(file, "Initial content");

            git.add()
                    .addFilepattern("README.md")
                    .call();

            git.commit()
                    .setMessage("Initial commit")
                    .setAuthor("Test User", "test@example.com")
                    .call();

            Files.writeString(file, "Updated content");

            git.add()
                    .addFilepattern("README.md")
                    .call();

            git.commit()
                    .setMessage("Updated README")
                    .setAuthor("Test User", "test@example.com")
                    .call();
        }

        GitHistoryScanner scanner = new GitHistoryScanner();

        List<GitHistoryScanner.CommitInfo> commits =
                scanner.getCommitHistory(tempDir);

        assertEquals(2, commits.size());
        assertNotNull(commits.get(0).commitHash());
        assertNotNull(commits.get(0).committedAt());
        assertNotNull(commits.get(0).author());
    }

    @Test
    void shouldDetectChangedFilesInCommit() throws Exception {

        try (Git git = Git.init()
                .setDirectory(tempDir.toFile())
                .call()) {

            Path file = tempDir.resolve("README.md");
            Files.writeString(file, "Initial content");

            git.add()
                    .addFilepattern("README.md")
                    .call();

            git.commit()
                    .setMessage("Initial commit")
                    .setAuthor("Test User", "test@example.com")
                    .call();

            Files.writeString(file, "Updated content");

            git.add()
                    .addFilepattern("README.md")
                    .call();

            RevCommit secondCommit = git.commit()
                    .setMessage("Update README")
                    .setAuthor("Test User", "test@example.com")
                    .call();

            GitHistoryScanner scanner = new GitHistoryScanner();

            List<GitHistoryScanner.ChangedFile> changedFiles =
                    scanner.getChangedFiles(tempDir, secondCommit.getName());

            assertEquals(1, changedFiles.size());
            assertEquals("MODIFY", changedFiles.get(0).changeType());
            assertEquals("README.md", changedFiles.get(0).newPath());
        }
    }

    @Test
    void shouldExtractOnlyAddedLinesFromCommit() throws Exception {

        String testToken =
                "ghp_ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";

        try (Git git = Git.init()
                .setDirectory(tempDir.toFile())
                .call()) {

            Path file = tempDir.resolve("config.properties");

            // First commit
            Files.writeString(file, "app.name=SecretGuard\n");

            git.add()
                    .addFilepattern("config.properties")
                    .call();

            git.commit()
                    .setMessage("Add configuration")
                    .setAuthor("Test User", "test@example.com")
                    .call();

            // Second commit: add a token
            Files.writeString(
                    file,
                    "app.name=SecretGuard\napi.token=" + testToken + "\n"
            );

            git.add()
                    .addFilepattern("config.properties")
                    .call();

            RevCommit secondCommit = git.commit()
                    .setMessage("Add API token")
                    .setAuthor("Test User", "test@example.com")
                    .call();

            GitHistoryScanner scanner = new GitHistoryScanner();

            List<GitHistoryScanner.AddedLine> addedLines =
                    scanner.getAddedLines(tempDir, secondCommit.getName());

            assertEquals(1, addedLines.size());

            assertEquals(
                    "config.properties",
                    addedLines.get(0).filePath()
            );

            assertEquals(2, addedLines.get(0).lineNumber());

            assertEquals(
                    "api.token=" + testToken,
                    addedLines.get(0).content()
            );
        }
    }
}
