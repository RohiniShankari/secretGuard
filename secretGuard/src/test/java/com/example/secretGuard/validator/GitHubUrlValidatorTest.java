package com.example.secretGuard.validator;



import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GitHubUrlValidatorTest {

    private final GitHubUrlValidator validator = new GitHubUrlValidator();

    @Test
    void shouldAcceptValidGitHubUrl() {
        assertTrue(validator.isValid(
                "https://github.com/rohini/secretguard"
        ));
    }

    @Test
    void shouldAcceptUrlWithTrailingSlash() {
        assertTrue(validator.isValid(
                "https://github.com/rohini/secretguard/"
        ));
    }

    @Test
    void shouldRejectNullOrEmptyUrl() {
        assertFalse(validator.isValid(null));
        assertFalse(validator.isValid(""));
        assertFalse(validator.isValid("   "));
    }

    @Test
    void shouldRejectInvalidDomain() {
        assertFalse(validator.isValid(
                "https://google.com/rohini/secretguard"
        ));
    }

    @Test
    void shouldRejectHttpUrl() {
        assertFalse(validator.isValid(
                "http://github.com/rohini/secretguard"
        ));
    }

    @Test
    void shouldRejectQueryString() {
        assertFalse(validator.isValid(
                "https://github.com/rohini/secretguard?tab=readme"
        ));
    }

    @Test
    void shouldRejectFragment() {
        assertFalse(validator.isValid(
                "https://github.com/rohini/secretguard#readme"
        ));
    }

    @Test
    void shouldRejectExtraPathSegments() {
        assertFalse(validator.isValid(
                "https://github.com/rohini/secretguard/tree/main"
        ));
    }

    @Test
    void shouldRejectGitCloneUrl() {
        assertFalse(validator.isValid(
                "https://github.com/rohini/secretguard.git"
        ));
    }

    @Test
    void shouldRejectCustomPort() {
        assertFalse(validator.isValid(
                "https://github.com:8080/rohini/secretguard"
        ));
    }

    @Test
    void shouldRejectUserInfo() {
        assertFalse(validator.isValid(
                "https://rohini@github.com/rohini/secretguard"
        ));
    }

    @Test
    void shouldRejectMissingPathSegments() {
        assertFalse(validator.isValid(
                "https://github.com/rohini"
        ));
    }
}