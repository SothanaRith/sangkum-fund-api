package com.example.digital_donation_api.repository;

import com.example.digital_donation_api.entity.Charity;
import com.example.digital_donation_api.entity.CharityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CharityRepository extends JpaRepository<Charity, Long> {

    Optional<Charity> findByOwnerId(Long userId);

    List<Charity> findByStatus(CharityStatus status);
}
