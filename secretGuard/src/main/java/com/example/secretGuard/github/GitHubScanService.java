package com.example.secretGuard.github;


import com.example.secretGuard.scanner.SecretScanner;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Service
public class GitHubScanService {

    private final GitHubRepositoryService repositoryService;
    private final SecretScanner secretScanner;

    public GitHubScanService(
            GitHubRepositoryService repositoryService,
            SecretScanner secretScanner) {
        this.repositoryService = repositoryService;
        this.secretScanner = secretScanner;
    }

    public List<SecretScanner.SecretFinding> scanGitHubRepository(
            String repositoryUrl) throws GitAPIException, IOException {

        Path repositoryPath = repositoryService.cloneRepository(repositoryUrl);

        try {
            return secretScanner.scanFiles(repositoryPath);
        } finally {
            repositoryService.deleteDirectory(repositoryPath.getParent());
        }
    }
}