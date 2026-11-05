package com.example.secretGuard.scanner;



import org.springframework.stereotype.Component;

@Component
public class RiskAnalyzer {

    public String calculateRisk(String severity, String confidence) {

        if ("CRITICAL".equals(severity)) {
            return "CRITICAL";
        }

        if ("HIGH".equals(severity) && "HIGH".equals(confidence)) {
            return "HIGH";
        }

        if ("HIGH".equals(severity) || "MEDIUM".equals(severity)) {
            return "MEDIUM";
        }

        return "LOW";
    }
}
