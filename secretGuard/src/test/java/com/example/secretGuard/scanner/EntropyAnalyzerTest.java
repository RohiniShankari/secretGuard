package com.example.secretGuard.scanner;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EntropyAnalyzerTest {

    private final EntropyAnalyzer analyzer = new EntropyAnalyzer();

    @Test
    void shouldCalculateZeroEntropyForRepeatedCharacters() {
        assertEquals(0.0,
                analyzer.calculateEntropy("aaaaaaaaaa"));
    }

    @Test
    void shouldCalculatePositiveEntropy() {
        assertTrue(analyzer.calculateEntropy("abcdefghij") > 0);
    }

    @Test
    void shouldDetectHighEntropyString() {
        assertTrue(analyzer.isHighEntropy(
                "aB9$xK2!mP7@qR4#tV8z"
        ));
    }

    @Test
    void shouldRejectLowEntropyString() {
        assertFalse(analyzer.isHighEntropy(
                "aaaaaaaaaaaaaaaaaaaa"
        ));
    }

    @Test
    void shouldRejectShortString() {
        assertFalse(analyzer.isHighEntropy("aB9$xK2!"));
    }

    @Test
    void shouldHandleNullInput() {
        assertFalse(analyzer.isHighEntropy(null));
    }
}
