package com.example.secretGuard.scanner;


import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SecretScannerTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldDetectGenericHighEntropySecret() throws Exception {

        Path file = tempDir.resolve("application.properties");

        Files.writeString(file,
                "custom.token=aB9xK2mP7qR4tV8zL5nC");

        SecretScanner scanner = new SecretScanner(
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

        List<SecretScanner.SecretFinding> findings =
                scanner.scanFiles(tempDir);

        assertEquals(1, findings.size());

        assertEquals(
                "GENERIC_HIGH_ENTROPY_SECRET",
                findings.get(0).secretType()
        );

        assertEquals(
                "aB9x********L5nC",
                findings.get(0).maskedValue()
        );
        assertEquals("MEDIUM", findings.get(0).confidence());
        assertEquals("MEDIUM", findings.get(0).severity());
        assertTrue(findings.get(0).explanation().contains("high entropy"));
        assertTrue(findings.get(0).explanation().contains("sensitive context (+25)"));
        assertTrue(findings.get(0).explanation().contains("high entropy (+15)"));
        assertEquals("MEDIUM", findings.get(0).risk());
        
    }
    
    @Test
    void shouldNotDetectLowEntropyValue() throws Exception {

        Path file = tempDir.resolve("application.properties");

        Files.writeString(file,
                "custom.token=aaaaaaaaaaaaaaaaaaaaaaaa");

        SecretScanner scanner = new SecretScanner(
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

        List<SecretScanner.SecretFinding> findings =
                scanner.scanFiles(tempDir);

        assertTrue(findings.isEmpty());
        
    }
    
    @Test
    void shouldAssignHighConfidenceToKnownSecretPattern() throws Exception {

        Path file = tempDir.resolve("aws.properties");

        Files.writeString(file,
                "aws.access.key=AKIAIOSFODNN7EXAMPLE");

        SecretScanner scanner = new SecretScanner(
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

        List<SecretScanner.SecretFinding> findings =
                scanner.scanFiles(tempDir);

        assertEquals(1, findings.size());
        assertEquals("HIGH", findings.get(0).confidence());
        assertEquals("HIGH", findings.get(0).severity());
        assertTrue(findings.get(0).explanation().contains("pattern match (+60)"));
        assertTrue(findings.get(0).explanation().contains("sensitive context (+25)"));
        assertEquals("HIGH", findings.get(0).risk());
    }
    
}