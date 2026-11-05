package com.example.secretGuard.scanner;

import org.springframework.stereotype.Component;

@Component
public class ConfidenceAnalyzer {

    public String calculateConfidence(
            boolean patternMatched,
            boolean sensitiveContext,
            boolean highEntropy) {

        int score = 0;

        if (patternMatched) {
            score += 60;
        }

        if (sensitiveContext) {
            score += 25;
        }

        if (highEntropy) {
            score += 15;
        }

        if (score >= 75) {
            return "HIGH";
        } else if (score >= 40) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }
    public int calculateConfidenceScore(
            boolean patternMatched,
            boolean sensitiveContext,
            boolean highEntropy) {

        int score = 0;

        if (patternMatched) score += 60;
        if (sensitiveContext) score += 25;
        if (highEntropy) score += 15;

        return score;
    }
}