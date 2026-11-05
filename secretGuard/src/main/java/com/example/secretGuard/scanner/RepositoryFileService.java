package com.example.secretGuard.scanner;


import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

@Service
public class RepositoryFileService {

    private static final long MAX_FILE_SIZE = 1024 * 1024;

    public List<Path> getFilesToScan(Path repositoryPath) throws IOException {

        try (Stream<Path> paths = Files.walk(repositoryPath)) {

            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> !path.startsWith(repositoryPath.resolve(".git")))
                    .filter(path -> {
                        try {
                            return Files.size(path) <= MAX_FILE_SIZE;
                        } catch (IOException e) {
                            return false;
                        }
                    })
                    .toList();
        }
    }
}