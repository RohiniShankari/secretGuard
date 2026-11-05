package com.example.secretGuard.scanner;


import java.util.regex.Pattern;

public class SecretPattern {

    private final String secretType;
    private final Pattern regex;
    private final String explanation;

    public SecretPattern(String secretType, String regex, String explanation) {
        this.secretType = secretType;
        this.regex = Pattern.compile(regex);
        this.explanation = explanation;
    }

    public String getSecretType() {
        return secretType;
    }

    public Pattern getRegex() {
        return regex;
    }

    public String getExplanation() {
        return explanation;
    }
}
