package com.example.digital_donation_api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Generic image upload endpoint used for uploading KHQR images, logos, etc.
 * Saves files to the uploads/images directory and returns the accessible URL.
 */
@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"},
             allowedHeaders = "*",
             methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
public class UploadController {

    @PostMapping("/image")
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file
    ) {
        Map<String, String> response = new HashMap<>();

        if (file.isEmpty()) {
            response.put("error", "No file provided");
            return ResponseEntity.badRequest().body(response);
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            response.put("error", "Only image files are allowed");
            return ResponseEntity.badRequest().body(response);
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            response.put("error", "File size must not exceed 5MB");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            String uploadDir = "uploads/images/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";
            String filename = UUID.randomUUID() + extension;

            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            String imageUrl = "http://localhost:8080/uploads/images/" + filename;
            response.put("url", imageUrl);
            response.put("filename", filename);
            response.put("message", "Image uploaded successfully");
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            response.put("error", "Failed to upload file: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
