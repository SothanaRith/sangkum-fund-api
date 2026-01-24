package com.example.digital_donation_api.service;

import com.example.digital_donation_api.dto.request.PaymentProofRequest;
import com.example.digital_donation_api.entity.PaymentProof;

import java.util.List;

public interface PaymentProofService {
    
    PaymentProof uploadProof(PaymentProofRequest request);
    
    PaymentProof getById(Long id);
    
    PaymentProof getByDonationId(Long donationId);
    
    List<PaymentProof> getPendingProofs();
    
    List<PaymentProof> getProofsByEventId(Long eventId);
    
    PaymentProof approveProof(Long proofId, Long adminId, String adminNotes);
    
    PaymentProof rejectProof(Long proofId, Long adminId, String adminNotes);
}
