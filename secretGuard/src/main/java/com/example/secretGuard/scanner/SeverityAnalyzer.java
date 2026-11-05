package com.example.secretGuard.scanner;



import org.springframework.stereotype.Component;

@Component
public class SeverityAnalyzer {

    public String calculateSeverity(String secretType) {

        return switch (secretType) {
            case "PRIVATE_KEY" -> "CRITICAL";

            case "AWS_ACCESS_KEY", "GITHUB_TOKEN" -> "HIGH";

            case "GENERIC_HIGH_ENTROPY_SECRET" -> "MEDIUM";

            default -> "LOW";
        };
    }
}