package com.example.digital_donation_api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_settings")
@Getter
@Setter
public class Settings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(columnDefinition = "JSON")
    private String privacySettings;

    @Column(columnDefinition = "JSON")
    private String notificationSettings;

    @Column(columnDefinition = "JSON")
    private String securitySettings;
}
