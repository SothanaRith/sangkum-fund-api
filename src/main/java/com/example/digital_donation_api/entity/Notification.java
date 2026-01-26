package com.example.digital_donation_api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type")
    private NotificationType type;

    @Column(columnDefinition = "JSON")
    private String data;

    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "is_dismissed")
    private Boolean isDismissed = false;

    @Column(name = "dismissed_at")
    private LocalDateTime dismissedAt;

    @Column(name = "action_url")
    private String actionUrl;

    @Column(name = "action_label")
    private String actionLabel;

    @Column(name = "related_id")
    private Long relatedId;

    @Column(name = "related_type")
    private String relatedType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Convenience methods
    public boolean isRead() {
        return isRead != null && isRead;
    }

    public boolean isDismissed() {
        return isDismissed != null && isDismissed;
    }
}

