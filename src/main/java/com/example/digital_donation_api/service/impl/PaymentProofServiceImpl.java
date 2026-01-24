package com.example.digital_donation_api.service.impl;

import com.example.digital_donation_api.dto.request.PaymentProofRequest;
import com.example.digital_donation_api.entity.*;
import com.example.digital_donation_api.exception.BadRequestException;
import com.example.digital_donation_api.exception.ResourceNotFoundException;
import com.example.digital_donation_api.repository.*;
import com.example.digital_donation_api.service.PaymentProofService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentProofServiceImpl implements PaymentProofService {

    private final PaymentProofRepository paymentProofRepository;
    private final DonationRepository donationRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final EventReportRepository eventReportRepository;

    @Override
    public PaymentProof uploadProof(PaymentProofRequest request) {
        Donation donation = donationRepository.findById(request.getDonationId())
                .orElseThrow(() -> new ResourceNotFoundException("Donation not found"));

        // Check if proof already exists
        if (paymentProofRepository.existsByDonationId(request.getDonationId())) {
            throw new BadRequestException("Payment proof already uploaded for this donation");
        }

        // Verify donation is for offline payment
        if (donation.getPaymentMethod() != PaymentMethod.OFFLINE_QR) {
            throw new BadRequestException("Payment proof only required for offline payments");
        }

        PaymentProof proof = new PaymentProof();
        proof.setDonation(donation);
        proof.setImageUrl(request.getImageUrl());
        proof.setNotes(request.getNotes());
        proof.setTransactionId(request.getTransactionId());
        proof.setBankName(request.getBankName());
        
        if (request.getTransactionDate() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            proof.setTransactionDate(LocalDateTime.parse(request.getTransactionDate(), formatter));
        }
        
        proof.setStatus(PaymentProofStatus.PENDING);
        proof.setUploadedAt(LocalDateTime.now());

        return paymentProofRepository.save(proof);
    }

    @Override
    public PaymentProof getById(Long id) {
        return paymentProofRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment proof not found"));
    }

    @Override
    public PaymentProof getByDonationId(Long donationId) {
        return paymentProofRepository.findByDonationId(donationId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment proof not found for this donation"));
    }

    @Override
    public List<PaymentProof> getPendingProofs() {
        return paymentProofRepository.findByStatus(PaymentProofStatus.PENDING);
    }

    @Override
    public List<PaymentProof> getProofsByEventId(Long eventId) {
        return paymentProofRepository.findByDonation_Event_Id(eventId);
    }

    @Override
    public PaymentProof approveProof(Long proofId, Long adminId, String adminNotes) {
        PaymentProof proof = getById(proofId);
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        proof.setStatus(PaymentProofStatus.APPROVED);
        proof.setVerifiedBy(admin);
        proof.setVerifiedAt(LocalDateTime.now());
        proof.setAdminNotes(adminNotes);

        // Update donation status to SUCCESS
        Donation donation = proof.getDonation();
        donation.setStatus(DonationStatus.SUCCESS);
        donationRepository.save(donation);

        // Update event totals
        Event event = donation.getEvent();
        event.setCurrentAmount(event.getCurrentAmount().add(donation.getAmount()));
        eventRepository.save(event);

        // Update event report
        updateEventReport(event, donation.getAmount());

        return paymentProofRepository.save(proof);
    }

    @Override
    public PaymentProof rejectProof(Long proofId, Long adminId, String adminNotes) {
        PaymentProof proof = getById(proofId);
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        proof.setStatus(PaymentProofStatus.REJECTED);
        proof.setVerifiedBy(admin);
        proof.setVerifiedAt(LocalDateTime.now());
        proof.setAdminNotes(adminNotes);

        // Update donation status to FAILED
        Donation donation = proof.getDonation();
        donation.setStatus(DonationStatus.FAILED);
        donationRepository.save(donation);

        return paymentProofRepository.save(proof);
    }

    private void updateEventReport(Event event, java.math.BigDecimal amount) {
        EventReport report = eventReportRepository
                .findByEventId(event.getId())
                .orElseGet(() -> {
                    EventReport r = new EventReport();
                    r.setEvent(event);
                    return r;
                });

        report.setTotalDonations(event.getCurrentAmount());
        report.setTotalDonors(donationRepository.countDonors(event.getId()).intValue());
        report.setLastUpdatedAt(LocalDateTime.now());

        eventReportRepository.save(report);
    }
}
