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

    long countByUserIdAndIsReadFalse(Long userId);

    // New admin notification methods
    Page<Notification> findByRead(boolean read, Pageable pageable);

    List<Notification> findByRead(boolean read);

    Page<Notification> findByType(NotificationType type, Pageable pageable);

    Page<Notification> findByReadAndType(boolean read, NotificationType type, Pageable pageable);

    Page<Notification> findByDismissed(boolean dismissed, Pageable pageable);

    // Notification counting
    long countByRead(boolean read);

    long countByDismissed(boolean dismissed);

    long countByType(NotificationType type);

    // Notification retrieval
    List<Notification> findAllByOrderByCreatedAtDesc();

    List<Notification> findByReadOrderByCreatedAtDesc(boolean read);
}
