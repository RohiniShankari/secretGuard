package com.example.secretGuard.validator;


import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;

@Component
public class GitHubUrlValidator {

    public boolean isValid(String url) {

        if (url == null || url.isBlank()) {
            return false;
        }

        URI uri;

        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            return false;
        }

        // Validate scheme and host
        if (!"https".equalsIgnoreCase(uri.getScheme())
                || !"github.com".equalsIgnoreCase(uri.getHost())) {
            return false;
        }

        // Reject user information, custom ports, query strings and fragments
        if (uri.getUserInfo() != null
                || uri.getPort() != -1
                || uri.getQuery() != null
                || uri.getFragment() != null) {
            return false;
        }

        String path = uri.getPath();

        if (path == null) {
            return false;
        }

        // Remove one optional trailing slash
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        String[] segments = path.split("/", -1);

        // Expected format: /owner/repository
        if (segments.length != 3) {
            return false;
        }

        String owner = segments[1];
        String repository = segments[2];

        // Validate owner
        if (!owner.matches("[a-zA-Z0-9-]+")) {
            return false;
        }

        // Validate repository
        if (!repository.matches("[a-zA-Z0-9._-]+")) {
            return false;
        }

        // Reject Git clone URLs
        return !repository.endsWith(".git");
    }
}