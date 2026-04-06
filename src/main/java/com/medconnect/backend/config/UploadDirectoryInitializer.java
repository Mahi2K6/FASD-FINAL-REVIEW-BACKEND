package com.medconnect.backend.config;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class UploadDirectoryInitializer {

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Path.of("uploads", "id-cards"));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create upload directory", e);
        }
    }
}
