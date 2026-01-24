package com.example.digital_donation_api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "telegram_integrations")
@Getter
@Setter
public class TelegramIntegration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String chatId;
    private Boolean isActive = true;

    private LocalDateTime createdAt = LocalDateTime.now();
}
