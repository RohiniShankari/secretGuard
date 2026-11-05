package com.example.secretGuard.scanner;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SecretPatternConfig {

    public List<SecretPattern> getPatterns() {

        return List.of(

            new SecretPattern(
                "AWS_ACCESS_KEY",
                "AKIA[0-9A-Z]{16}",
                "Matches the format of an AWS access key ID."
            ),

            new SecretPattern(
                "GITHUB_TOKEN",
                "gh[pousr]_[A-Za-z0-9]{36}",
                "Matches the format of a GitHub personal access token."
            ),

            new SecretPattern(
                "PRIVATE_KEY",
                "-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----",
                "Detects a private key header."
            )

        );
    }
}
