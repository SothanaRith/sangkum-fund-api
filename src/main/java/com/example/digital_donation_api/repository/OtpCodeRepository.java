package com.example.digital_donation_api.repository;

import com.example.digital_donation_api.entity.OtpCode;
import com.example.digital_donation_api.entity.OtpType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {
    
    Optional<OtpCode> findByEmailAndCodeAndTypeAndVerifiedFalse(String email, String code, OtpType type);
    
    Optional<OtpCode> findFirstByEmailAndTypeAndVerifiedFalseOrderByCreatedAtDesc(String email, OtpType type);
    
    @Modifying
    @Query("DELETE FROM OtpCode o WHERE o.expiryTime < ?1")
    void deleteExpiredOtps(LocalDateTime now);
    
    @Modifying
    @Query("DELETE FROM OtpCode o WHERE o.email = ?1 AND o.type = ?2 AND o.verified = false")
    void deleteUnverifiedOtpsByEmailAndType(String email, OtpType type);
}
