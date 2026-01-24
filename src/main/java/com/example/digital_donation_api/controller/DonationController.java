package com.example.digital_donation_api.controller;

import com.example.digital_donation_api.dto.mapper.DonationMapper;
import com.example.digital_donation_api.dto.request.DonationRequest;
import com.example.digital_donation_api.dto.response.DonationResponse;
import com.example.digital_donation_api.dto.response.PaymentResponse;
import com.example.digital_donation_api.entity.Donation;
import com.example.digital_donation_api.entity.User;
import com.example.digital_donation_api.service.DonationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/donations")
@RequiredArgsConstructor
public class DonationController {

    private final DonationService donationService;

    @PostMapping
    public ResponseEntity<PaymentResponse> makeDonation(
            @Valid @RequestBody DonationRequest request,
            Authentication authentication
    ) {
        // TODO: Get user ID from authentication - for now use placeholder
        Long userId = 1L; // Replace with actual user ID from authentication
        
        Donation donation = donationService.donate(
                request.getEventId(),
                userId,
                request.getAmount(),
                request.getAnonymous(),
                request.getPaymentMethod(),
                request.getPaymentDetails()
        );
        
        // Create payment response with appropriate message
        String message = getPaymentMessage(donation);
        
        PaymentResponse response = new PaymentResponse(
                donation.getId(),
                donation.getStatus().name(),
                donation.getPaymentMethod().name(),
                donation.getTransactionRef(),
                donation.getQrCodeData(),
                message
        );
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/my-donations")
    public ResponseEntity<List<DonationResponse>> getMyDonations(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<Donation> donations = donationService.getMyDonations(user.getId());
        List<DonationResponse> responses = donations.stream()
                .map(DonationMapper::toResponse)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<DonationResponse> getDonation(@PathVariable Long id) {
        // TODO: Implement get donation by ID
        return ResponseEntity.ok(null);
    }
    
    @PostMapping("/{id}/confirm")
    public ResponseEntity<PaymentResponse> confirmPayment(@PathVariable Long id) {
        // TODO: Implement payment confirmation for KHQR/OFFLINE_QR
        // This endpoint would be called after QR code payment is verified
        return ResponseEntity.ok(null);
    }
    
    private String getPaymentMessage(Donation donation) {
        switch (donation.getPaymentMethod()) {
            case VISA_CARD:
                return "Payment processed successfully via Visa card";
            case KHQR:
                return "Please scan the KHQR code to complete payment";
            case OFFLINE_QR:
                return "Please scan the QR code and complete payment offline. Payment will be verified by admin.";
            default:
                return "Donation created successfully";
        }
    }
}
