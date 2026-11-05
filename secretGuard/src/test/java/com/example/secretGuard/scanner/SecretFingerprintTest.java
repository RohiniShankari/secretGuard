
package com.example.secretGuard.scanner;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SecretFingerprintTest {

    private final SecretFingerprint fingerprint =
            new SecretFingerprint();

    @Test
    void shouldGenerateSameFingerprintForSameSecret() {

        String first = fingerprint.generate("my-test-secret-123");
        String second = fingerprint.generate("my-test-secret-123");

        assertEquals(first, second);
    }

    @Test
    void shouldGenerateDifferentFingerprintsForDifferentSecrets() {

        String first = fingerprint.generate("secret-one");
        String second = fingerprint.generate("secret-two");

        assertNotEquals(first, second);
    }

    @Test
    void shouldGenerateSha256LengthFingerprint() {

        String result = fingerprint.generate("my-test-secret");

        assertEquals(64, result.length());
    }
}
