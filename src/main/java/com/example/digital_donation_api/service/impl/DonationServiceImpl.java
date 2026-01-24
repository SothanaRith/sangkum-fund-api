package com.example.digital_donation_api.service.impl;

import com.example.digital_donation_api.dto.request.PaymentDetailsRequest;
import com.example.digital_donation_api.entity.*;
import com.example.digital_donation_api.exception.ResourceNotFoundException;
import com.example.digital_donation_api.repository.*;
import com.example.digital_donation_api.service.DonationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DonationServiceImpl implements DonationService {

    private final DonationRepository donationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventReportRepository eventReportRepository;

    @Override
    public Donation donate(Long eventId, Long userId, BigDecimal amount, boolean anonymous,
                          PaymentMethod paymentMethod, PaymentDetailsRequest paymentDetails) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        User user = anonymous ? null :
                userRepository.findById(userId).orElseThrow();

        Donation donation = new Donation();
        donation.setEvent(event);
        donation.setUser(user);
        donation.setAmount(amount);
        donation.setIsAnonymous(anonymous);
        donation.setPaymentMethod(paymentMethod);
        
        // Process payment based on method
        String transactionRef = processPayment(paymentMethod, amount, paymentDetails);
        donation.setTransactionRef(transactionRef);
        
        // Generate QR code for KHQR or OFFLINE_QR
        if (paymentMethod == PaymentMethod.KHQR || paymentMethod == PaymentMethod.OFFLINE_QR) {
            String qrCodeData = generateQRCodeData(paymentMethod, amount, transactionRef, paymentDetails);
            donation.setQrCodeData(qrCodeData);
        }
        
        // Set status based on payment method
        if (paymentMethod == PaymentMethod.VISA_CARD) {
            donation.setStatus(DonationStatus.SUCCESS); // Assume instant for card
        } else {
            donation.setStatus(DonationStatus.PENDING); // Wait for QR payment confirmation
        }

        donationRepository.save(donation);

        // Update event total only if payment is successful
        if (donation.getStatus() == DonationStatus.SUCCESS) {
            updateEventTotals(event, amount, eventId);
        }

        return donation;
    }
    
    @Override
    public List<Donation> getMyDonations(Long userId) {
        return donationRepository.findByUserId(userId);
    }
    
    private String processPayment(PaymentMethod method, BigDecimal amount, PaymentDetailsRequest details) {
        String transactionRef = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        switch (method) {
            case VISA_CARD:
                // TODO: Integrate with actual payment gateway (Stripe, etc.)
                // For now, simulate successful payment
                return transactionRef;
                
            case KHQR:
                // TODO: Integrate with KHQR payment system
                // Generate KHQR transaction reference
                return "KHQR-" + transactionRef;
                
            case OFFLINE_QR:
                // Generate offline QR code reference
                return "OFFLINE-" + transactionRef;
                
            default:
                throw new IllegalArgumentException("Invalid payment method");
        }
    }
    
    private String generateQRCodeData(PaymentMethod method, BigDecimal amount, 
                                     String transactionRef, PaymentDetailsRequest details) {
        // Generate QR code data string
        // For KHQR: Use Bakong KHQR format
        // For OFFLINE_QR: Generate custom format with payment info
        
        StringBuilder qrData = new StringBuilder();
        qrData.append("PAYMENT_METHOD:").append(method.name()).append("|");
        qrData.append("AMOUNT:").append(amount).append("|");
        qrData.append("TXN_REF:").append(transactionRef).append("|");
        
        if (details != null && details.getPhoneNumber() != null) {
            qrData.append("PHONE:").append(details.getPhoneNumber()).append("|");
        }
        
        qrData.append("TIMESTAMP:").append(System.currentTimeMillis());
        
        // TODO: Generate actual QR code image and return base64 or URL
        // For now, return the data string that can be encoded into QR
        return qrData.toString();
    }
    
    private void updateEventTotals(Event event, BigDecimal amount, Long eventId) {
        // Update event total
        event.setCurrentAmount(event.getCurrentAmount().add(amount));

        // Update report
        EventReport report = eventReportRepository
                .findByEventId(eventId)
                .orElseGet(() -> {
                    EventReport r = new EventReport();
                    r.setEvent(event);
                    return r;
                });

        report.setTotalDonations(event.getCurrentAmount());
        report.setTotalDonors(donationRepository.countDonors(eventId).intValue());
        report.setLastUpdatedAt(LocalDateTime.now());

        eventReportRepository.save(report);
    }
}
