package com.example.secretGuard.controller;


import com.example.secretGuard.github.GitHubScanService;
import com.example.secretGuard.scanner.SecretScanner.SecretFinding;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/github-scan")
public class GitHubScanController {

    private final GitHubScanService gitHubScanService;

    public GitHubScanController(GitHubScanService gitHubScanService) {
        this.gitHubScanService = gitHubScanService;
    }

    @PostMapping
    public ResponseEntity<?> scanRepository(@RequestBody Map<String, String> request) {

        String repositoryUrl = request.get("repositoryUrl");

        try {
            List<SecretFinding> findings =
                    gitHubScanService.scanGitHubRepository(repositoryUrl);

            return ResponseEntity.ok(findings);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));

        } catch (GitAPIException | IOException e) {

            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "errorType", e.getClass().getName(),
                        "errorMessage", String.valueOf(e.getMessage())
                    ));
        }
    }
}
