package com.example.digital_donation_api.controller;

import com.example.digital_donation_api.dto.mapper.PaymentProofMapper;
import com.example.digital_donation_api.dto.request.PaymentProofRequest;
import com.example.digital_donation_api.dto.response.PaymentProofResponse;
import com.example.digital_donation_api.entity.PaymentProof;
import com.example.digital_donation_api.service.PaymentProofService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payment-proofs")
@RequiredArgsConstructor
public class PaymentProofController {

    private final PaymentProofService paymentProofService;

    @PostMapping
    public ResponseEntity<PaymentProofResponse> uploadProof(
            @Valid @RequestBody PaymentProofRequest request,
            Authentication authentication
    ) {
        PaymentProof proof = paymentProofService.uploadProof(request);
        return ResponseEntity.ok(PaymentProofMapper.toResponse(proof));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentProofResponse> getProof(@PathVariable Long id) {
        PaymentProof proof = paymentProofService.getById(id);
        return ResponseEntity.ok(PaymentProofMapper.toResponse(proof));
    }

    @GetMapping("/donation/{donationId}")
    public ResponseEntity<PaymentProofResponse> getProofByDonation(@PathVariable Long donationId) {
        PaymentProof proof = paymentProofService.getByDonationId(donationId);
        return ResponseEntity.ok(PaymentProofMapper.toResponse(proof));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentProofResponse>> getPendingProofs() {
        List<PaymentProof> proofs = paymentProofService.getPendingProofs();
        List<PaymentProofResponse> responses = proofs.stream()
                .map(PaymentProofMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/event/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentProofResponse>> getProofsByEvent(@PathVariable Long eventId) {
        List<PaymentProof> proofs = paymentProofService.getProofsByEventId(eventId);
        List<PaymentProofResponse> responses = proofs.stream()
                .map(PaymentProofMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentProofResponse> approveProof(
            @PathVariable Long id,
            @RequestParam(required = false) String notes,
            Authentication authentication
    ) {
        // TODO: Get actual admin ID from authentication
        Long adminId = 1L;
        
        PaymentProof proof = paymentProofService.approveProof(id, adminId, notes);
        return ResponseEntity.ok(PaymentProofMapper.toResponse(proof));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentProofResponse> rejectProof(
            @PathVariable Long id,
            @RequestParam(required = false) String notes,
            Authentication authentication
    ) {
        // TODO: Get actual admin ID from authentication
        Long adminId = 1L;
        
        PaymentProof proof = paymentProofService.rejectProof(id, adminId, notes);
        return ResponseEntity.ok(PaymentProofMapper.toResponse(proof));
    }
}
