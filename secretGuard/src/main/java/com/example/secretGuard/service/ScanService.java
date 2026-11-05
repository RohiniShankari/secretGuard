package com.example.secretGuard.service;

import com.example.secretGuard.entity.Scan;
import com.example.secretGuard.repository.ScanRepository;
import com.example.secretGuard.validator.GitHubUrlValidator;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

//@Service
public class ScanService {

    private final ScanRepository scanRepository;
    private final GitHubUrlValidator gitHubUrlValidator;

    public ScanService(
            ScanRepository scanRepository,
            GitHubUrlValidator gitHubUrlValidator) {

        this.scanRepository = scanRepository;
        this.gitHubUrlValidator = gitHubUrlValidator;
    }

    public Scan createScan(String repositoryName, String sourceType) {

        if ("GITHUB_URL".equalsIgnoreCase(sourceType)
                && !gitHubUrlValidator.isValid(repositoryName)) {

            throw new IllegalArgumentException("Invalid GitHub repository URL");
        }

        Scan scan = new Scan();

        scan.setRepositoryName(repositoryName);
        scan.setSourceType(sourceType);
        scan.setStatus("PENDING");

        return scanRepository.save(scan);
    }

    public List<Scan> getAllScans() {
        return scanRepository.findAll();
    }

    public Optional<Scan> getScanById(Long id) {
        return scanRepository.findById(id);
    }
}