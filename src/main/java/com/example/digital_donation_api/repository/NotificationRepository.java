package com.example.digital_donation_api.repository;

import com.example.digital_donation_api.entity.Notification;
import com.example.digital_donation_api.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Original methods
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    Page<Notification> findByUserId(Long userId, Pageable pageable);

    long countByUserIdAndIsReadFalse(Long userId);

    // New admin notification methods
    Page<Notification> findByIsRead(boolean isRead, Pageable pageable);

    List<Notification> findByIsRead(boolean isRead);

    Page<Notification> findByType(NotificationType type, Pageable pageable);

    Page<Notification> findByIsReadAndType(boolean isRead, NotificationType type, Pageable pageable);

    Page<Notification> findByIsDismissed(boolean isDismissed, Pageable pageable);

    // Notification counting
    long countByIsRead(boolean isRead);

    long countByIsDismissed(boolean isDismissed);

    long countByType(NotificationType type);

    // Notification retrieval
    List<Notification> findAllByOrderByCreatedAtDesc();

    List<Notification> findByIsReadOrderByCreatedAtDesc(boolean isRead);
}
