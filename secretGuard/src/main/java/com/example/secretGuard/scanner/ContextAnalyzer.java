package com.example.secretGuard.scanner;


import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class ContextAnalyzer {

    private static final Pattern SENSITIVE_KEYWORDS = Pattern.compile(
            "(?i)\\b(password|passwd|secret|token|api[._-]?key|"
            + "access[._-]?key|client[._-]?secret|"
            + "private[._-]?key|credential)\\b"
    );

    public boolean hasSensitiveContext(String line) {
        if (line == null || line.isBlank()) {
            return false;
        }

        return SENSITIVE_KEYWORDS.matcher(line).find();
    }
}
