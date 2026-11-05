
package com.example.secretGuard.scanner;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Service
public class SecretScanner {

    private final SecretPatternConfig patternConfig;
    private final RepositoryFileService fileService;
    private final PlaceholderDetector placeholderDetector;
    private final ContextAnalyzer contextAnalyzer;
    private final EntropyAnalyzer entropyAnalyzer;
    private final ConfidenceAnalyzer confidenceAnalyzer;
    private final SeverityAnalyzer severityAnalyzer;
    private final RiskAnalyzer riskAnalyzer;

    private final SecretFingerprint secretFingerprint;
    private static final Pattern TOKEN_CANDIDATE =
            Pattern.compile("(?<==)[A-Za-z0-9_+/=-]{20,}");

    public SecretScanner(
            SecretPatternConfig patternConfig,
            RepositoryFileService fileService,
            PlaceholderDetector placeholderDetector,
            ContextAnalyzer contextAnalyzer,
            EntropyAnalyzer entropyAnalyzer,
            ConfidenceAnalyzer confidenceAnalyzer,
            SeverityAnalyzer severityAnalyzer,
            RiskAnalyzer riskAnalyzer,
            SecretFingerprint secretFingerprint) {

        this.patternConfig = patternConfig;
        this.fileService = fileService;
        this.placeholderDetector = placeholderDetector;
        this.contextAnalyzer = contextAnalyzer;
        this.entropyAnalyzer = entropyAnalyzer;
        this.confidenceAnalyzer = confidenceAnalyzer;
        this.severityAnalyzer = severityAnalyzer;
        this.riskAnalyzer = riskAnalyzer;
        this.secretFingerprint = secretFingerprint;
    }

    // Scan files in the current repository snapshot.
    public List<SecretFinding> scanFiles(Path repositoryPath)
            throws IOException {

        List<Path> files = fileService.getFilesToScan(repositoryPath);
        List<SecretFinding> findings = new ArrayList<>();

        for (Path file : files) {

            List<String> lines;

            try {
                lines = Files.readAllLines(
                        file,
                        StandardCharsets.UTF_8
                );
            } catch (MalformedInputException e) {
                continue;
            }

            String filePath =
                    repositoryPath.relativize(file).toString();

            for (int i = 0; i < lines.size(); i++) {

                findings.addAll(
                        scanLine(lines.get(i), filePath, i + 1)
                );
            }
        }

        sortFindings(findings);

        return findings;
    }

    // Scan lines supplied from Git history.
    public List<SecretFinding> scanLines(List<ScanLine> lines) {

        List<SecretFinding> findings = new ArrayList<>();

        for (ScanLine line : lines) {

            findings.addAll(
                    scanLine(
                            line.content(),
                            line.filePath(),
                            line.lineNumber()
                    )
            );
        }

        sortFindings(findings);

        return findings;
    }

    // Shared detection logic used by both scanning methods.
    private List<SecretFinding> scanLine(
            String line, String filePath, int lineNumber) {

        List<SecretFinding> findings = new ArrayList<>();

        Set<String> knownMatches = new HashSet<>();

        // Pattern-based detection.
        for (SecretPattern pattern : patternConfig.getPatterns()) {

            Matcher matcher = pattern.getRegex().matcher(line);

            while (matcher.find()) {

                String secret = matcher.group();
                String fingerprint = secretFingerprint.generate(secret);

                knownMatches.add(secret);

                if (placeholderDetector.isPlaceholder(secret)) {
                    continue;
                }

                String maskedSecret = maskSecret(secret);

                boolean sensitiveContext =
                        contextAnalyzer.hasSensitiveContext(line);

                boolean highEntropy =
                        entropyAnalyzer.isHighEntropy(secret);

                String confidence =
                        confidenceAnalyzer.calculateConfidence(
                                true,
                                sensitiveContext,
                                highEntropy
                        );

                int confidenceScore =
                        confidenceAnalyzer.calculateConfidenceScore(
                                true,
                                sensitiveContext,
                                highEntropy
                        );

                String severity =
                        severityAnalyzer.calculateSeverity(
                                pattern.getSecretType()
                        );

                String risk =
                        riskAnalyzer.calculateRisk(severity, confidence);

                String explanation = pattern.getExplanation();

                if (sensitiveContext) {
                    explanation +=
                            " Found in a sensitive configuration context.";
                } else {
                    explanation +=
                            " No sensitive keyword was found in the surrounding line.";
                }

                if (highEntropy) {
                    explanation += " The value also has high entropy.";
                }

                explanation +=
                        " Confidence signals: pattern match (+60)";

                if (sensitiveContext) {
                    explanation += ", sensitive context (+25)";
                }

                if (highEntropy) {
                    explanation += ", high entropy (+15)";
                }

                explanation += ".";

                findings.add(new SecretFinding(
                        pattern.getSecretType(),
                        maskedSecret,
                        filePath,
                        lineNumber,
                        explanation,
                        confidence,
                        confidenceScore,
                        severity,
                        risk,
                        fingerprint
                    
                ));
            }
        }

        // Generic entropy-based detection.
        boolean sensitiveContext =
                contextAnalyzer.hasSensitiveContext(line);

        if (sensitiveContext) {

            Matcher candidateMatcher =
                    TOKEN_CANDIDATE.matcher(line);

            while (candidateMatcher.find()) {

                String candidate = candidateMatcher.group();
                String fingerprint = secretFingerprint.generate(candidate);

                if (knownMatches.contains(candidate)) {
                    continue;
                }

                if (placeholderDetector.isPlaceholder(candidate)) {
                    continue;
                }

                if (!entropyAnalyzer.isHighEntropy(candidate)) {
                    continue;
                }

                String genericConfidence =
                        confidenceAnalyzer.calculateConfidence(
                                false,
                                sensitiveContext,
                                true
                        );

                int genericConfidenceScore =
                        confidenceAnalyzer.calculateConfidenceScore(
                                false,
                                sensitiveContext,
                                true
                        );

                String genericSeverity =
                        severityAnalyzer.calculateSeverity(
                                "GENERIC_HIGH_ENTROPY_SECRET"
                        );

                String genericRisk =
                        riskAnalyzer.calculateRisk(
                                genericSeverity,
                                genericConfidence
                        );

                String genericExplanation =
                        "Long value detected with high entropy. "
                        + "Found in a sensitive configuration context. "
                        + "Confidence signals: sensitive context (+25), "
                        + "high entropy (+15).";

                findings.add(new SecretFinding(
                        "GENERIC_HIGH_ENTROPY_SECRET",
                        maskSecret(candidate),
                        filePath,
                        lineNumber,
                        genericExplanation,
                        genericConfidence,
                        genericConfidenceScore,
                        genericSeverity,
                        genericRisk,
                        fingerprint
                ));
            }
        }

        return findings;
    }

    // Sort findings by risk priority.
    private void sortFindings(List<SecretFinding> findings) {

        findings.sort(
                Comparator.comparingInt(finding -> switch (finding.risk()) {
                    case "CRITICAL" -> 1;
                    case "HIGH" -> 2;
                    case "MEDIUM" -> 3;
                    case "LOW" -> 4;
                    default -> 5;
                })
        );
    }

    private String maskSecret(String secret) {

        if (secret.length() <= 8) {
            return "********";
        }

        return secret.substring(0, 4)
                + "********"
                + secret.substring(secret.length() - 4);
    }

    // Input format for scanning individual lines.
    public record ScanLine(
            String filePath,
            int lineNumber,
            String content
    ) {}

    public record SecretFinding(
            String secretType,
            String maskedValue,
            String filePath,
            int lineNumber,
            String explanation,
            String confidence,
            int confidenceScore,
            String severity,
            String risk,
            @JsonIgnore String fingerprint
    ) {}
}
