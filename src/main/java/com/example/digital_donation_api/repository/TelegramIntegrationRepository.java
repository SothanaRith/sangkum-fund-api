package com.example.digital_donation_api.repository;

import com.example.digital_donation_api.entity.TelegramIntegration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TelegramIntegrationRepository
        extends JpaRepository<TelegramIntegration, Long> {

    Optional<TelegramIntegration> findByUserId(Long userId);

    boolean existsByUserIdAndIsActiveTrue(Long userId);
}
