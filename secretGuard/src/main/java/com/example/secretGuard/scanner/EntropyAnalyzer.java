package com.example.secretGuard.scanner;


import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class EntropyAnalyzer {

    private static final int MIN_LENGTH = 20;
    private static final double ENTROPY_THRESHOLD = 3.5;

    public double calculateEntropy(String value) {

        if (value == null || value.isEmpty()) {
            return 0.0;
        }

        Map<Character, Integer> frequencies = new HashMap<>();

        for (char c : value.toCharArray()) {
            frequencies.put(c, frequencies.getOrDefault(c, 0) + 1);
        }

        double entropy = 0.0;
        int length = value.length();

        for (int frequency : frequencies.values()) {

            double probability = (double) frequency / length;

            entropy -= probability * (Math.log(probability) / Math.log(2));
        }

        return entropy;
    }

    public boolean isHighEntropy(String value) {

        if (value == null || value.length() < MIN_LENGTH) {
            return false;
        }

        return calculateEntropy(value) >= ENTROPY_THRESHOLD;
    }
}