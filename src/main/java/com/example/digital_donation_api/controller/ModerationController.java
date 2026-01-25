package com.example.digital_donation_api.controller;

import com.example.digital_donation_api.entity.Charity;
import com.example.digital_donation_api.entity.Event;
import com.example.digital_donation_api.entity.User;
import com.example.digital_donation_api.service.ModerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/moderation")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ModerationController {

    private final ModerationService moderationService;

    // ==================== User Moderation ====================
    
    @PostMapping("/users/{userId}/block")
    public ResponseEntity<Map<String, Object>> blockUser(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        
        String reason = request.get("reason");
        if (reason == null || reason.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Block reason is required"
            ));
        }
        
        Long adminId = 1L; // TODO: Get actual admin ID from authentication
        
        User user = moderationService.blockUser(userId, adminId, reason);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "User blocked successfully");
        response.put("userId", user.getId());
        response.put("email", user.getEmail());
        response.put("isBlocked", user.getIsBlocked());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/users/{userId}/unblock")
    public ResponseEntity<Map<String, Object>> unblockUser(
            @PathVariable Long userId,
            Authentication authentication) {
        
        Long adminId = 1L; // TODO: Get actual admin ID from authentication
        
        User user = moderationService.unblockUser(userId, adminId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "User unblocked successfully");
        response.put("userId", user.getId());
        response.put("email", user.getEmail());
        response.put("isBlocked", user.getIsBlocked());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{userId}/status")
    public ResponseEntity<Map<String, Object>> getUserBlockStatus(@PathVariable Long userId) {
        boolean isBlocked = moderationService.isUserBlocked(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("isBlocked", isBlocked);
        
        return ResponseEntity.ok(response);
    }

    // ==================== Event Moderation ====================
    
    @PostMapping("/events/{eventId}/block")
    public ResponseEntity<Map<String, Object>> blockEvent(
            @PathVariable Long eventId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        
        String reason = request.get("reason");
        if (reason == null || reason.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Block reason is required"
            ));
        }
        
        Long adminId = 1L; // TODO: Get actual admin ID from authentication
        
        Event event = moderationService.blockEvent(eventId, adminId, reason);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Event blocked successfully");
        response.put("eventId", event.getId());
        response.put("title", event.getTitle());
        response.put("status", event.getStatus());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/events/{eventId}/unblock")
    public ResponseEntity<Map<String, Object>> unblockEvent(
            @PathVariable Long eventId,
            Authentication authentication) {
        
        Long adminId = 1L; // TODO: Get actual admin ID from authentication
        
        Event event = moderationService.unblockEvent(eventId, adminId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Event unblocked successfully");
        response.put("eventId", event.getId());
        response.put("title", event.getTitle());
        response.put("status", event.getStatus());
        
        return ResponseEntity.ok(response);
    }

    // ==================== Charity Moderation ====================
    
    @PostMapping("/charities/{charityId}/block")
    public ResponseEntity<Map<String, Object>> blockCharity(
            @PathVariable Long charityId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        
        String reason = request.get("reason");
        if (reason == null || reason.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Block reason is required"
            ));
        }
        
        Long adminId = 1L; // TODO: Get actual admin ID from authentication
        
        Charity charity = moderationService.blockCharity(charityId, adminId, reason);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Charity blocked successfully");
        response.put("charityId", charity.getId());
        response.put("name", charity.getName());
        response.put("status", charity.getStatus());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/charities/{charityId}/unblock")
    public ResponseEntity<Map<String, Object>> unblockCharity(
            @PathVariable Long charityId,
            Authentication authentication) {
        
        Long adminId = 1L; // TODO: Get actual admin ID from authentication
        
        Charity charity = moderationService.unblockCharity(charityId, adminId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Charity unblocked successfully");
        response.put("charityId", charity.getId());
        response.put("name", charity.getName());
        response.put("status", charity.getStatus());
        
        return ResponseEntity.ok(response);
    }
}
