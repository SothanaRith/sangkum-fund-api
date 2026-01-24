package com.example.digital_donation_api.repository;

import com.example.digital_donation_api.entity.Settings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserSettingRepository extends JpaRepository<Settings, Long> {

    Optional<Settings> findByUserId(Long userId);
}
