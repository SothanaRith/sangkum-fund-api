package com.example.digital_donation_api.service.impl;

import com.example.digital_donation_api.entity.Notification;
import com.example.digital_donation_api.entity.NotificationType;
import com.example.digital_donation_api.repository.NotificationRepository;
import com.example.digital_donation_api.repository.UserRepository;
import com.example.digital_donation_api.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    public void notifyUser(Long userId, String type, String data) {
        userRepository.findById(userId).ifPresent(user -> {
            Notification notification = new Notification();
            notification.setUser(user);
            try {
                notification.setType(NotificationType.valueOf(type.toUpperCase()));
            } catch (IllegalArgumentException e) {
                notification.setType(NotificationType.INFO);
            }
            notification.setData(data);
            notificationRepository.save(notification);
        });
    }

    @Override
    public void notify(Long userId, String title, String message, String type, String actionUrl, Long relatedId, String relatedType) {
        userRepository.findById(userId).ifPresent(user -> {
            Notification notification = new Notification();
            notification.setUser(user);
            notification.setTitle(title);
            notification.setMessage(message);
            try {
                notification.setType(NotificationType.valueOf(type.toUpperCase()));
            } catch (IllegalArgumentException e) {
                notification.setType(NotificationType.INFO);
            }
            notification.setActionUrl(actionUrl);
            notification.setRelatedId(relatedId);
            notification.setRelatedType(relatedType);
            notification.setIsRead(false);
            notificationRepository.save(notification);
        });
    }
}
