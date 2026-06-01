package com.example.digital_donation_api.controller;

import com.example.digital_donation_api.entity.Notification;
import com.example.digital_donation_api.entity.NotificationType;
import com.example.digital_donation_api.exception.ResourceNotFoundException;
import com.example.digital_donation_api.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminNotificationController {

    private final NotificationRepository notificationRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Boolean read,
            @RequestParam(required = false) String type
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Notification> notificationPage;

        if (read != null && type != null) {
            NotificationType nt = parseType(type);
            notificationPage = nt != null
                    ? notificationRepository.findByIsReadAndType(read, nt, pageable)
                    : notificationRepository.findByIsRead(read, pageable);
        } else if (read != null) {
            notificationPage = notificationRepository.findByIsRead(read, pageable);
        } else if (type != null) {
            NotificationType nt = parseType(type);
            notificationPage = nt != null
                    ? notificationRepository.findByType(nt, pageable)
                    : Page.empty(pageable);
        } else {
            notificationPage = notificationRepository.findAll(pageable);
        }

        return ResponseEntity.ok(pageResponse(notificationPage));
    }

    @GetMapping("/pending")
    public ResponseEntity<Map<String, Object>> getPendingNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Notification> pending = notificationRepository.findByIsRead(false, pageable);

        Map<String, Object> response = pageResponse(pending);
        response.put("unreadCount", notificationRepository.countByIsRead(false));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/system")
    public ResponseEntity<Map<String, Object>> getSystemNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Notification> systemNotifications = notificationRepository.findByType(NotificationType.SYSTEM, pageable);
        return ResponseEntity.ok(pageResponse(systemNotifications));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<Map<String, Object>> getNotificationsByType(
            @PathVariable String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size
    ) {
        NotificationType notificationType = NotificationType.valueOf(type.toUpperCase());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Notification> result = notificationRepository.findByType(notificationType, pageable);

        Map<String, Object> response = pageResponse(result);
        response.put("type", type);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{notificationId}/read")
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable Long notificationId) {
        Notification notif = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        notif.setIsRead(true);
        notif.setReadAt(LocalDateTime.now());
        notificationRepository.save(notif);
        return ResponseEntity.ok(Map.of("success", true, "message", "Notification marked as read", "id", notificationId));
    }

    @PostMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead() {
        List<Notification> unread = notificationRepository.findByIsRead(false);
        unread.forEach(n -> {
            n.setIsRead(true);
            n.setReadAt(LocalDateTime.now());
        });
        notificationRepository.saveAll(unread);
        return ResponseEntity.ok(Map.of("success", true, "message", "All notifications marked as read", "count", unread.size()));
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Map<String, Object>> deleteNotification(@PathVariable Long notificationId) {
        if (!notificationRepository.existsById(notificationId)) {
            throw new ResourceNotFoundException("Notification not found");
        }
        notificationRepository.deleteById(notificationId);
        return ResponseEntity.ok(Map.of("success", true, "message", "Notification deleted", "id", notificationId));
    }

    @PostMapping("/{notificationId}/dismiss")
    public ResponseEntity<Map<String, Object>> dismissNotification(@PathVariable Long notificationId) {
        Notification notif = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        notif.setIsDismissed(true);
        notif.setDismissedAt(LocalDateTime.now());
        notificationRepository.save(notif);
        return ResponseEntity.ok(Map.of("success", true, "message", "Notification dismissed", "id", notificationId));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getNotificationStats() {
        long total = notificationRepository.count();
        long unread = notificationRepository.countByIsRead(false);
        long dismissed = notificationRepository.countByIsDismissed(true);

        Map<String, Long> byType = new HashMap<>();
        for (NotificationType t : NotificationType.values()) {
            byType.put(t.name(), notificationRepository.countByType(t));
        }

        return ResponseEntity.ok(Map.of(
                "totalNotifications", total,
                "unreadCount", unread,
                "dismissedCount", dismissed,
                "readCount", total - unread - dismissed,
                "typeCount", byType
        ));
    }

    @GetMapping("/recent")
    public ResponseEntity<Map<String, Object>> getRecentNotifications(
            @RequestParam(defaultValue = "5") int limit
    ) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by("createdAt").descending());
        Page<Notification> recent = notificationRepository.findAll(pageable);
        return ResponseEntity.ok(pageResponse(recent));
    }

    private NotificationType parseType(String type) {
        try {
            return NotificationType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Map<String, Object> pageResponse(Page<Notification> page) {
        Map<String, Object> response = new HashMap<>();
        response.put("content", page.getContent().stream().map(this::mapNotification).toList());
        response.put("pageNumber", page.getNumber());
        response.put("pageSize", page.getSize());
        response.put("totalElements", page.getTotalElements());
        response.put("totalPages", page.getTotalPages());
        return response;
    }

    private Map<String, Object> mapNotification(Notification n) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", n.getId());
        map.put("title", n.getTitle());
        map.put("message", n.getMessage());
        map.put("type", n.getType());
        map.put("read", n.isRead());
        map.put("dismissed", n.isDismissed());
        map.put("actionUrl", n.getActionUrl());
        map.put("actionLabel", n.getActionLabel());
        map.put("createdAt", n.getCreatedAt());
        map.put("readAt", n.getReadAt());
        map.put("relatedId", n.getRelatedId());
        map.put("relatedType", n.getRelatedType());
        return map;
    }
}
