package com.example.digital_donation_api.controller;

import com.example.digital_donation_api.dto.response.NotificationResponse;
import com.example.digital_donation_api.entity.Notification;
import com.example.digital_donation_api.entity.User;
import com.example.digital_donation_api.repository.NotificationRepository;
import com.example.digital_donation_api.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationController(NotificationRepository notificationRepository, 
                                 UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getNotifications(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        long unreadCount = notificationRepository.countByUserIdAndIsReadFalse(user.getId());

        List<NotificationResponse> notificationResponses = notifications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("notifications", notificationResponses);
        response.put("unreadCount", unreadCount);
        response.put("success", true);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable Long id, 
                                                          Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body(Map.of(
                "success", false,
                "message", "Unauthorized"
            ));
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("notification", mapToResponse(notification));

        return ResponseEntity.ok(response);
    }

    @PutMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        notifications.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(notifications);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "All notifications marked as read");

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteNotification(@PathVariable Long id,
                                                                   Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body(Map.of(
                "success", false,
                "message", "Unauthorized"
            ));
        }

        notificationRepository.delete(notification);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Notification deleted"
        ));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadCount(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        long unreadCount = notificationRepository.countByUserIdAndIsReadFalse(user.getId());

        return ResponseEntity.ok(Map.of(
            "success", true,
            "unreadCount", unreadCount
        ));
    }

    private NotificationResponse mapToResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setType(notification.getType().name());
        response.setData(notification.getData());
        response.setIsRead(notification.getIsRead());
        response.setCreatedAt(notification.getCreatedAt().toString());
        return response;
    }
}
