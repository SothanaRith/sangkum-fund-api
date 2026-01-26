package com.example.digital_donation_api.controller;

import com.example.digital_donation_api.dto.mapper.UserMapper;
import com.example.digital_donation_api.dto.response.UserResponse;
import com.example.digital_donation_api.entity.User;
import com.example.digital_donation_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserRepository userRepository;

    /**
     * Get all registered users with pagination
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search
    ) {
        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            Page<User> userPage;
            if (search != null && !search.isEmpty()) {
                userPage = userRepository.findBySearch(search.toLowerCase(), pageable);
            } else {
                userPage = userRepository.findAll(pageable);
            }

            List<UserResponse> responses = userPage.getContent().stream()
                    .map(UserMapper::toResponse)
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("content", responses);
            response.put("pageNumber", userPage.getNumber());
            response.put("pageSize", userPage.getSize());
            response.put("totalElements", userPage.getTotalElements());
            response.put("totalPages", userPage.getTotalPages());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to fetch users: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get user by ID
     */
    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long userId) {
        try {
            Optional<User> user = userRepository.findById(userId);

            if (user.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "User not found");
                return ResponseEntity.status(404).body(errorResponse);
            }

            User userEntity = user.get();
            UserResponse userResponse = UserMapper.toResponse(userEntity);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", userResponse);
            response.put("lastLoginAt", userEntity.getLastLoginAt());
            response.put("createdAt", userEntity.getCreatedAt());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error fetching user: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Activate user account
     */
    @PostMapping("/{userId}/activate")
    public ResponseEntity<Map<String, Object>> activateUser(@PathVariable Long userId) {
        try {
            Optional<User> user = userRepository.findById(userId);

            if (user.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "User not found");
                return ResponseEntity.status(404).body(errorResponse);
            }

            User userEntity = user.get();
            userEntity.setIsActive(true);
            userEntity.setIsBlocked(false);
            userRepository.save(userEntity);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User activated successfully");
            response.put("userId", userId);
            response.put("isActive", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error activating user: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Deactivate user account
     */
    @PostMapping("/{userId}/deactivate")
    public ResponseEntity<Map<String, Object>> deactivateUser(@PathVariable Long userId) {
        try {
            Optional<User> user = userRepository.findById(userId);

            if (user.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "User not found");
                return ResponseEntity.status(404).body(errorResponse);
            }

            User userEntity = user.get();
            userEntity.setIsActive(false);
            userRepository.save(userEntity);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User deactivated successfully");
            response.put("userId", userId);
            response.put("isActive", false);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error deactivating user: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Block user account
     */
    @PostMapping("/{userId}/block")
    public ResponseEntity<Map<String, Object>> blockUser(@PathVariable Long userId) {
        try {
            Optional<User> user = userRepository.findById(userId);

            if (user.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "User not found");
                return ResponseEntity.status(404).body(errorResponse);
            }

            User userEntity = user.get();
            userEntity.setIsBlocked(true);
            userEntity.setIsActive(false);
            userRepository.save(userEntity);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User blocked successfully");
            response.put("userId", userId);
            response.put("isBlocked", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error blocking user: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Unblock user account
     */
    @PostMapping("/{userId}/unblock")
    public ResponseEntity<Map<String, Object>> unblockUser(@PathVariable Long userId) {
        try {
            Optional<User> user = userRepository.findById(userId);

            if (user.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "User not found");
                return ResponseEntity.status(404).body(errorResponse);
            }

            User userEntity = user.get();
            userEntity.setIsBlocked(false);
            userEntity.setFailedLoginAttempts(0);
            userRepository.save(userEntity);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User unblocked successfully");
            response.put("userId", userId);
            response.put("isBlocked", false);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error unblocking user: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Search users by username or email
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchUsers(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<User> userPage;

            String searchQuery = q != null ? q.toLowerCase() : "";

            if (!searchQuery.isEmpty()) {
                userPage = userRepository.findBySearch(searchQuery, pageable);
            } else {
                userPage = userRepository.findAll(pageable);
            }

            List<UserResponse> responses = userPage.getContent().stream()
                    .map(UserMapper::toResponse)
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("query", q);
            response.put("content", responses);
            response.put("totalResults", userPage.getTotalElements());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Search failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get active users only
     */
    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("lastLoginAt").descending());
            Page<User> activeUsers = userRepository.findByIsActive(true, pageable);

            List<UserResponse> responses = activeUsers.getContent().stream()
                    .map(UserMapper::toResponse)
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("content", responses);
            response.put("count", activeUsers.getTotalElements());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error fetching active users: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Get blocked users only
     */
    @GetMapping("/blocked")
    public ResponseEntity<Map<String, Object>> getBlockedUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<User> blockedUsers = userRepository.findByIsBlocked(true, pageable);

            List<UserResponse> responses = blockedUsers.getContent().stream()
                    .map(UserMapper::toResponse)
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("content", responses);
            response.put("count", blockedUsers.getTotalElements());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error fetching blocked users: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Get user statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        try {
            long totalUsers = userRepository.count();
            long activeUsers = userRepository.countByIsActive(true);
            long inactiveUsers = userRepository.countByIsActive(false);
            long blockedUsers = userRepository.countByIsBlocked(true);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("totalUsers", totalUsers);
            response.put("activeUsers", activeUsers);
            response.put("inactiveUsers", inactiveUsers);
            response.put("blockedUsers", blockedUsers);
            response.put("activePercentage", totalUsers > 0 ? (activeUsers * 100.0 / totalUsers) : 0);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error calculating statistics: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Get recent users
     */
    @GetMapping("/recent")
    public ResponseEntity<Map<String, Object>> getRecentUsers(
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            Pageable pageable = PageRequest.of(0, limit, Sort.by("createdAt").descending());
            Page<User> recentUsers = userRepository.findAll(pageable);

            List<UserResponse> responses = recentUsers.getContent().stream()
                    .map(UserMapper::toResponse)
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("content", responses);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error fetching recent users: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
