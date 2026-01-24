package com.example.digital_donation_api.dto.mapper;

import com.example.digital_donation_api.dto.response.PaymentProofResponse;
import com.example.digital_donation_api.entity.PaymentProof;

public class PaymentProofMapper {
    
    public static PaymentProofResponse toResponse(PaymentProof proof) {
        return new PaymentProofResponse(
                proof.getId(),
                proof.getDonation().getId(),
                proof.getImageUrl(),
                proof.getNotes(),
                proof.getTransactionId(),
                proof.getBankName(),
                proof.getTransactionDate(),
                proof.getStatus().name(),
                proof.getUploadedAt(),
                proof.getVerifiedBy() != null ? proof.getVerifiedBy().getName() : null,
                proof.getVerifiedAt(),
                proof.getAdminNotes()
        );
    }
}
