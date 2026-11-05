package com.example.secretGuard.github;


import com.example.secretGuard.validator.GitHubUrlValidator;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.stereotype.Service;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.FileVisitResult;
import java.nio.file.attribute.BasicFileAttributes;

@Service
public class GitHubRepositoryService {

    private final GitHubUrlValidator urlValidator;

    public GitHubRepositoryService(GitHubUrlValidator urlValidator) {
        this.urlValidator = urlValidator;
    }

    public Path cloneRepository(String repositoryUrl)
            throws GitAPIException, IOException {

        if (!urlValidator.isValid(repositoryUrl)) {
            throw new IllegalArgumentException("Invalid GitHub repository URL");
        }

        Path tempDirectory = Files.createTempDirectory("secretguard-");
        Path repositoryDirectory = tempDirectory.resolve("repository");

        try (Git git = Git.cloneRepository()
                .setURI(repositoryUrl)
                .setDirectory(repositoryDirectory.toFile())
                .call()) {

            return repositoryDirectory;

        } catch (GitAPIException e) {

            try {
                deleteDirectory(tempDirectory);
            } catch (IOException cleanupException) {
                e.addSuppressed(cleanupException);
            }

            throw e;
        }
    }
    public void deleteDirectory(Path directory) throws IOException {

        if (directory == null || !Files.exists(directory)) {
            return;
        }

        Files.walkFileTree(directory, new SimpleFileVisitor<>() {

            @Override
            public FileVisitResult visitFile(
                    Path file, BasicFileAttributes attrs) throws IOException {

                IOException lastException = null;

                for (int attempt = 1; attempt <= 3; attempt++) {
                    try {
                        file.toFile().setWritable(true);
                        Files.deleteIfExists(file);
                        return FileVisitResult.CONTINUE;

                    } catch (IOException e) {
                        lastException = e;

                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException interruptedException) {
                            Thread.currentThread().interrupt();
                            throw new IOException(interruptedException);
                        }
                    }
                }

                throw lastException;
            }

            @Override
            public FileVisitResult postVisitDirectory(
                    Path dir, IOException exc) throws IOException {

                if (exc != null) {
                    throw exc;
                }

                Files.deleteIfExists(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
