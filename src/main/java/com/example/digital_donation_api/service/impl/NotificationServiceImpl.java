package com.example.digital_donation_api.service.impl;

import com.example.digital_donation_api.entity.Notification;
import com.example.digital_donation_api.entity.User;
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
        User user = userRepository.findById(userId).orElseThrow();

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setData(data);

        notificationRepository.save(notification);
    }
}
