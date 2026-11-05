package com.example.secretGuard.scanner;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContextAnalyzerTest {

    private final ContextAnalyzer analyzer = new ContextAnalyzer();

    @Test
    void shouldDetectPasswordContext() {
        assertTrue(analyzer.hasSensitiveContext("password=abc123"));
    }

    @Test
    void shouldDetectTokenContext() {
        assertTrue(analyzer.hasSensitiveContext("github.token=abc123"));
    }

    @Test
    void shouldDetectApiKeyContext() {
        assertTrue(analyzer.hasSensitiveContext("api_key=abc123"));
    }

    @Test
    void shouldDetectPrivateKeyContext() {
        assertTrue(analyzer.hasSensitiveContext("private-key=abc123"));
    }

    @Test
    void shouldNotDetectNormalVariable() {
        assertFalse(analyzer.hasSensitiveContext("username=rohini"));
    }

    @Test
    void shouldHandleNullInput() {
        assertFalse(analyzer.hasSensitiveContext(null));
    }
}