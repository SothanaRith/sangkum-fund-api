package com.example.digital_donation_api.controller;

import com.example.digital_donation_api.entity.Event;
import com.example.digital_donation_api.entity.EventImage;
import com.example.digital_donation_api.entity.User;
import com.example.digital_donation_api.repository.EventImageRepository;
import com.example.digital_donation_api.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@RestController
@RequestMapping("/api/events/{eventId}/images")
@RequiredArgsConstructor
public class EventImageController {

    private final EventRepository eventRepository;
    private final EventImageRepository eventImageRepository;

    @PostMapping
    public ResponseEntity<?> uploadImages(
            @PathVariable Long eventId,
            @RequestParam("files") MultipartFile[] files,
            Authentication authentication
    ) {
        try {
            User user = (User) authentication.getPrincipal();
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new RuntimeException("Event not found"));

            // Verify ownership
            if (!event.getOwner().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Only event owner can upload images"));
            }

            if (files == null || files.length == 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "No files provided"));
            }

            // Create upload directory
            String uploadDir = "uploads/events/" + eventId + "/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            List<Map<String, String>> uploadedImages = new ArrayList<>();
            
            // Get current max display order
            List<EventImage> existingImages = eventImageRepository.findByEventIdOrderByDisplayOrderAsc(eventId);
            int maxOrder = existingImages.isEmpty() ? 0 : 
                existingImages.stream().mapToInt(EventImage::getDisplayOrder).max().orElse(0);

            for (int i = 0; i < files.length; i++) {
                MultipartFile file = files[i];
                
                if (file.isEmpty()) {
                    continue;
                }

                // Validate file type
                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    continue;
                }

                // Generate unique filename
                String originalFilename = file.getOriginalFilename();
                String extension = originalFilename != null && originalFilename.contains(".") 
                    ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                    : ".jpg";
                String filename = UUID.randomUUID().toString() + extension;

                // Save file
                Path filePath = uploadPath.resolve(filename);
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                // Save to database
                EventImage eventImage = new EventImage();
                eventImage.setEvent(event);
                eventImage.setImageUrl("http://localhost:8080/uploads/events/" + eventId + "/" + filename);
                eventImage.setDisplayOrder(maxOrder + i + 1);
                eventImage.setIsPrimary(existingImages.isEmpty() && i == 0); // First image of first upload is primary
                
                eventImageRepository.save(eventImage);

                Map<String, String> imageInfo = new HashMap<>();
                imageInfo.put("id", eventImage.getId().toString());
                imageInfo.put("url", eventImage.getImageUrl());
                imageInfo.put("isPrimary", String.valueOf(eventImage.getIsPrimary()));
                uploadedImages.add(imageInfo);
            }

            return ResponseEntity.ok(Map.of(
                "message", "Images uploaded successfully",
                "images", uploadedImages
            ));

        } catch (IOException e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to upload images: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<EventImage>> getEventImages(@PathVariable Long eventId) {
        List<EventImage> images = eventImageRepository.findByEventIdOrderByDisplayOrderAsc(eventId);
        return ResponseEntity.ok(images);
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<?> deleteImage(
            @PathVariable Long eventId,
            @PathVariable Long imageId,
            Authentication authentication
    ) {
        try {
            User user = (User) authentication.getPrincipal();
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new RuntimeException("Event not found"));

            // Verify ownership
            if (!event.getOwner().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Only event owner can delete images"));
            }

            EventImage image = eventImageRepository.findById(imageId)
                    .orElseThrow(() -> new RuntimeException("Image not found"));

            if (!image.getEvent().getId().equals(eventId)) {
                return ResponseEntity.status(403).body(Map.of("error", "Image does not belong to this event"));
            }

            // Delete file from filesystem
            String url = image.getImageUrl();
            if (url.contains("uploads/events/")) {
                String filePath = url.substring(url.indexOf("uploads/"));
                try {
                    Files.deleteIfExists(Paths.get(filePath));
                } catch (IOException e) {
                    // Log but don't fail if file deletion fails
                    System.err.println("Failed to delete file: " + e.getMessage());
                }
            }

            eventImageRepository.delete(image);

            return ResponseEntity.ok(Map.of("message", "Image deleted successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to delete image: " + e.getMessage()));
        }
    }

    @PutMapping("/{imageId}/primary")
    public ResponseEntity<?> setPrimaryImage(
            @PathVariable Long eventId,
            @PathVariable Long imageId,
            Authentication authentication
    ) {
        try {
            User user = (User) authentication.getPrincipal();
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new RuntimeException("Event not found"));

            // Verify ownership
            if (!event.getOwner().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Only event owner can set primary image"));
            }

            // Remove primary flag from all images
            List<EventImage> allImages = eventImageRepository.findByEventIdOrderByDisplayOrderAsc(eventId);
            for (EventImage img : allImages) {
                img.setIsPrimary(false);
                eventImageRepository.save(img);
            }

            // Set new primary image
            EventImage image = eventImageRepository.findById(imageId)
                    .orElseThrow(() -> new RuntimeException("Image not found"));
            
            if (!image.getEvent().getId().equals(eventId)) {
                return ResponseEntity.status(403).body(Map.of("error", "Image does not belong to this event"));
            }

            image.setIsPrimary(true);
            eventImageRepository.save(image);

            return ResponseEntity.ok(Map.of("message", "Primary image set successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to set primary image: " + e.getMessage()));
        }
    }
}
