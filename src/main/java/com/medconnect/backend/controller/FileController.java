package com.medconnect.backend.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
public class FileController {

    @PostMapping("/api/files/upload")
    public ResponseEntity<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is required");
        }
        try {
            Path uploadDir = Paths.get("uploads");
            Files.createDirectories(uploadDir);
            String original = file.getOriginalFilename();
            String extension = "";
            if (original != null) {
                int idx = original.lastIndexOf('.');
                if (idx >= 0) {
                    extension = original.substring(idx);
                }
            }
            String saved = UUID.randomUUID() + extension;
            Path target = uploadDir.resolve(saved);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/files/")
                    .path(saved)
                    .toUriString();
            Map<String, String> body = new HashMap<>();
            body.put("url", url);
            return ResponseEntity.ok(body);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "File upload failed", ex);
        }
    }

    @GetMapping("/api/files/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            Path file = Paths.get("uploads").resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            if ((!resource.exists() || !resource.isReadable())) {
                Path legacy = Paths.get("uploads/id-cards").resolve(filename);
                resource = new UrlResource(legacy.toUri());
            }
            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                        .body(resource);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
