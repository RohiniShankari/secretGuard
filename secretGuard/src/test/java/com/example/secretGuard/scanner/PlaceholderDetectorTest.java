package com.example.secretGuard.scanner;



import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlaceholderDetectorTest {

    private final PlaceholderDetector detector = new PlaceholderDetector();

    @Test
    void shouldIdentifyYourTokenAsPlaceholder() {
        assertTrue(detector.isPlaceholder("YOUR_GITHUB_TOKEN_HERE"));
    }

    @Test
    void shouldIdentifyChangeMeAsPlaceholder() {
        assertTrue(detector.isPlaceholder("CHANGE_ME"));
    }

    @Test
    void shouldIdentifyDummyAsPlaceholder() {
        assertTrue(detector.isPlaceholder("DUMMY_SECRET"));
    }

    @Test
    void shouldNotIdentifyNormalTokenAsPlaceholder() {
        assertFalse(detector.isPlaceholder(
                "ghp_AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
        ));
    }

    @Test
    void shouldNotIdentifyAwsTestValueAsPlaceholder() {
        assertFalse(detector.isPlaceholder("AKIAIOSFODNN7EXAMPLE"));
    }
}