package com.example.secretGuard.scanner;



import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class PlaceholderDetector {

    private static final List<String> PLACEHOLDER_WORDS = List.of(
            "YOUR_",
            "PLACEHOLDER",
            "CHANGE_ME",
            "CHANGEME",
            "INSERT_",
            "REPLACE_",
            "DUMMY",
            "FAKE",
            "TEST_"
    );

    public boolean isPlaceholder(String value) {

        if (value == null || value.isBlank()) {
            return true;
        }

        String normalizedValue = value.toUpperCase(Locale.ROOT);

        for (String word : PLACEHOLDER_WORDS) {
            if (normalizedValue.contains(word)) {
                return true;
            }
        }

        return false;
    }
}
