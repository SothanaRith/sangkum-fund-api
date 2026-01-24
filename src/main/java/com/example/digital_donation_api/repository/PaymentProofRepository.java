package com.example.digital_donation_api.repository;

import com.example.digital_donation_api.entity.PaymentProof;
import com.example.digital_donation_api.entity.PaymentProofStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentProofRepository extends JpaRepository<PaymentProof, Long> {

    Optional<PaymentProof> findByDonationId(Long donationId);
    
    List<PaymentProof> findByStatus(PaymentProofStatus status);
    
    List<PaymentProof> findByDonation_Event_Id(Long eventId);
    
    boolean existsByDonationId(Long donationId);
}
