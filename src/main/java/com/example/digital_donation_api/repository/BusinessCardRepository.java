package com.example.digital_donation_api.repository;

import com.example.digital_donation_api.entity.BusinessCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BusinessCardRepository extends JpaRepository<BusinessCard, Long> {

    Optional<BusinessCard> findByShareSlug(String shareSlug);

    Optional<BusinessCard> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
