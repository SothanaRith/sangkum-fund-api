package com.example.digital_donation_api.controller;

import com.example.digital_donation_api.entity.Notification;
import com.example.digital_donation_api.entity.NotificationType;
import com.example.digital_donation_api.repository.NotificationRepository;
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
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminNotificationController {

    private final NotificationRepository notificationRepository;

    /**
     * Get all notifications with pagination
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Boolean read,
            @RequestParam(required = false) String type
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Notification> notificationPage;

            if (read != null && type != null) {
                try {
                    NotificationType notificationType = NotificationType.valueOf(type.toUpperCase());
                    notificationPage = notificationRepository.findByReadAndType(read, notificationType, pageable);
                } catch (IllegalArgumentException e) {
                    notificationPage = notificationRepository.findByRead(read, pageable);
                }
            } else if (read != null) {
                notificationPage = notificationRepository.findByRead(read, pageable);
            } else if (type != null) {
                try {
                    NotificationType notificationType = NotificationType.valueOf(type.toUpperCase());
                    notificationPage = notificationRepository.findByType(notificationType, pageable);
                } catch (IllegalArgumentException e) {
                    notificationPage = Page.empty(pageable);
                }
            } else {
                notificationPage = notificationRepository.findAll(pageable);
            }

            List<Map<String, Object>> responses = notificationPage.getContent().stream()
                    .map(this::mapNotification)
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("content", responses);
            response.put("pageNumber", notificationPage.getNumber());
            response.put("pageSize", notificationPage.getSize());
            response.put("totalElements", notificationPage.getTotalElements());
            response.put("totalPages", notificationPage.getTotalPages());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to fetch notifications: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get pending notifications only
     */
    @GetMapping("/pending")
    public ResponseEntity<Map<String, Object>> getPendingNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Notification> pendingNotifications = notificationRepository.findByRead(false, pageable);

            List<Map<String, Object>> responses = pendingNotifications.getContent().stream()
                    .map(this::mapNotification)
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("content", responses);
            response.put("count", pendingNotifications.getTotalElements());
            response.put("unreadCount", notificationRepository.countByRead(false));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error fetching pending notifications: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Get system notifications
     */
    @GetMapping("/system")
    public ResponseEntity<Map<String, Object>> getSystemNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Notification> systemNotifications = notificationRepository.findByType(NotificationType.SYSTEM, pageable);

            List<Map<String, Object>> responses = systemNotifications.getContent().stream()
                    .map(this::mapNotification)
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("content", responses);
            response.put("count", systemNotifications.getTotalElements());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error fetching system notifications: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Get notifications by type
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<Map<String, Object>> getNotificationsByType(
            @PathVariable String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size
    ) {
        try {
            NotificationType notificationType = NotificationType.valueOf(type.toUpperCase());
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Notification> typeNotifications = notificationRepository.findByType(notificationType, pageable);

            List<Map<String, Object>> responses = typeNotifications.getContent().stream()
                    .map(this::mapNotification)
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("type", type);
            response.put("content", responses);
            response.put("count", typeNotifications.getTotalElements());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Invalid notification type: " + type);
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error fetching notifications: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Mark notification as read
     */
    @PostMapping("/{notificationId}/read")
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable Long notificationId) {
        try {
            Optional<Notification> notification = notificationRepository.findById(notificationId);

            if (notification.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Notification not found");
                return ResponseEntity.status(404).body(errorResponse);
            }

            Notification notif = notification.get();
            notif.setIsRead(true);
            notif.setReadAt(LocalDateTime.now());
            notificationRepository.save(notif);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Notification marked as read");
            response.put("id", notificationId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error marking notification as read: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Mark all notifications as read
     */
    @PostMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead() {
        try {
            List<Notification> unreadNotifications = notificationRepository.findByRead(false);
            unreadNotifications.forEach(notif -> {
                notif.setIsRead(true);
                notif.setReadAt(LocalDateTime.now());
            });
            notificationRepository.saveAll(unreadNotifications);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "All notifications marked as read");
            response.put("count", unreadNotifications.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error marking all notifications as read: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Delete notification
     */
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Map<String, Object>> deleteNotification(@PathVariable Long notificationId) {
        try {
            Optional<Notification> notification = notificationRepository.findById(notificationId);

            if (notification.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Notification not found");
                return ResponseEntity.status(404).body(errorResponse);
            }

            notificationRepository.deleteById(notificationId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Notification deleted");
            response.put("id", notificationId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error deleting notification: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Dismiss notification (soft delete - for archiving)
     */
    @PostMapping("/{notificationId}/dismiss")
    public ResponseEntity<Map<String, Object>> dismissNotification(@PathVariable Long notificationId) {
        try {
            Optional<Notification> notification = notificationRepository.findById(notificationId);

            if (notification.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Notification not found");
                return ResponseEntity.status(404).body(errorResponse);
            }

            Notification notif = notification.get();
            notif.setIsDismissed(true);
            notif.setDismissedAt(LocalDateTime.now());
            notificationRepository.save(notif);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Notification dismissed");
            response.put("id", notificationId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error dismissing notification: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Get notification statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getNotificationStats() {
        try {
            long totalNotifications = notificationRepository.count();
            long unreadCount = notificationRepository.countByRead(false);
            long dismissedCount = notificationRepository.countByDismissed(true);

            Map<String, Long> typeCount = new HashMap<>();
            for (NotificationType type : NotificationType.values()) {
                typeCount.put(type.name(), notificationRepository.countByType(type));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("totalNotifications", totalNotifications);
            response.put("unreadCount", unreadCount);
            response.put("dismissedCount", dismissedCount);
            response.put("readCount", totalNotifications - unreadCount - dismissedCount);
            response.put("typeCount", typeCount);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error calculating statistics: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Get recent notifications
     */
    @GetMapping("/recent")
    public ResponseEntity<Map<String, Object>> getRecentNotifications(
            @RequestParam(defaultValue = "5") int limit
    ) {
        try {
            Pageable pageable = PageRequest.of(0, limit, Sort.by("createdAt").descending());
            Page<Notification> recentNotifications = notificationRepository.findAll(pageable);

            List<Map<String, Object>> responses = recentNotifications.getContent().stream()
                    .map(this::mapNotification)
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("content", responses);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error fetching recent notifications: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Map notification entity to response DTO
     */
    private Map<String, Object> mapNotification(Notification notification) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", notification.getId());
        map.put("title", notification.getTitle());
        map.put("message", notification.getMessage());
        map.put("type", notification.getType());
        map.put("read", notification.isRead());
        map.put("dismissed", notification.isDismissed());
        map.put("actionUrl", notification.getActionUrl());
        map.put("actionLabel", notification.getActionLabel());
        map.put("createdAt", notification.getCreatedAt());
        map.put("readAt", notification.getReadAt());
        map.put("relatedId", notification.getRelatedId());
        map.put("relatedType", notification.getRelatedType());
        return map;
    }
}
