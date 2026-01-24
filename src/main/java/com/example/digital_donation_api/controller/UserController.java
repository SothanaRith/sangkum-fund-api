package com.example.digital_donation_api.controller;

import com.example.digital_donation_api.dto.mapper.UserMapper;
import com.example.digital_donation_api.dto.request.ProfileUpdateRequest;
import com.example.digital_donation_api.dto.response.UserResponse;
import com.example.digital_donation_api.entity.User;
import com.example.digital_donation_api.service.UserService;
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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        User fullUser = userService.getById(user.getId());
        return ResponseEntity.ok(UserMapper.toResponse(fullUser));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(
            @RequestBody ProfileUpdateRequest request,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        User updatedUser = userService.updateProfile(
                user.getId(),
                request.getName(),
                request.getAvatar(),
                request.getPhone()
        );
        return ResponseEntity.ok(UserMapper.toResponse(updatedUser));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        User user = userService.getById(id);
        return ResponseEntity.ok(UserMapper.toResponse(user));
    }

    @PostMapping("/profile/avatar")
    public ResponseEntity<Map<String, String>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        try {
            if (file.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Please select a file to upload");
                return ResponseEntity.badRequest().body(error);
            }

            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Only image files are allowed");
                return ResponseEntity.badRequest().body(error);
            }

            // Create upload directory if it doesn't exist
            String uploadDir = "uploads/avatars/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
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

            // Generate URL with backend host (in production, use proper domain)
            String avatarUrl = "http://localhost:8080/uploads/avatars/" + filename;

            // Update user avatar
            User user = (User) authentication.getPrincipal();
            User updatedUser = userService.updateProfile(
                    user.getId(),
                    null, // Don't change name
                    avatarUrl,
                    null  // Don't change phone
            );

            Map<String, String> response = new HashMap<>();
            response.put("avatarUrl", avatarUrl);
            response.put("message", "Avatar uploaded successfully");
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to upload file: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}
