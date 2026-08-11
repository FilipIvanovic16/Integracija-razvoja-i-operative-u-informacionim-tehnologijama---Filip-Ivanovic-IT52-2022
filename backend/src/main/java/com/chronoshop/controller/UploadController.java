package com.chronoshop.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class UploadController {

    @Value("${app.upload.dir:/app/uploads}")
    private String uploadDir;

    @PostMapping("/uploads")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> upload(@RequestParam("file") MultipartFile file) throws IOException {
        String original = file.getOriginalFilename();
        String ext = (original != null && original.contains("."))
                ? original.substring(original.lastIndexOf('.'))
                : ".jpg";
        String filename = UUID.randomUUID() + ext;
        Path dir = Path.of(uploadDir);
        Files.createDirectories(dir);
        file.transferTo(dir.resolve(filename));
        return ResponseEntity.ok(Map.of("url", "/api/uploads/" + filename));
    }
}
