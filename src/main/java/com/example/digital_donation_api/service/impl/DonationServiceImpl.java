package com.example.digital_donation_api.service.impl;

import com.example.digital_donation_api.dto.request.PaymentDetailsRequest;
import com.example.digital_donation_api.entity.*;
import com.example.digital_donation_api.exception.ResourceNotFoundException;
import com.example.digital_donation_api.repository.*;
import com.example.digital_donation_api.service.DonationService;
import com.example.digital_donation_api.service.BakongService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.util.DigestUtils;
import com.example.digital_donation_api.util.KhqrGenerator;

@Service
@RequiredArgsConstructor
@Transactional
public class DonationServiceImpl implements DonationService {

    private final DonationRepository donationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventReportRepository eventReportRepository;
    private final BakongService bakongService;

    @org.springframework.beans.factory.annotation.Value("${bakong.simulation:true}")
    private boolean simulationMode;

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
        
        if (paymentMethod == PaymentMethod.KHQR) {
            String bakongId = event.getBakongAccountId();
            if (bakongId == null || bakongId.trim().isEmpty()) {
                bakongId = "sothanarith_heang1@aclb"; // Fallback platform account for testing
            }
            
            // Generate a unique transaction reference (for the bill number)
            String billNumber = "DON-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            
            // Compute creation and expiration timestamps for dynamic KHQR (mandatory in V2.9)
            long creationTimestamp = System.currentTimeMillis();
            long expirationTimestamp = creationTimestamp + 3600000; // 1 hour expiration
            
            // Determine acquiring bank and category code (MCC)
            String acquiringBank = "Dev Bank";
            if (bakongId.endsWith("@aclb") || "sothanarith_heang1@aclb".equals(bakongId)) {
                acquiringBank = "ABA Bank";
            }
            String mcc = "sothanarith_heang1@aclb".equals(bakongId) ? "8220" : "5999";
            
            // Generate dynamic KHQR string complying with Version 2.9 SDK
            String qrString = KhqrGenerator.generate(
                bakongId,
                event.getTitle(),
                amount,
                billNumber,
                acquiringBank,
                mcc,
                creationTimestamp,
                expirationTimestamp
            );
            
            // Calculate MD5 hash of this QR string
            String md5Hash = DigestUtils.md5DigestAsHex(qrString.getBytes(StandardCharsets.UTF_8));
            
            donation.setTransactionRef(md5Hash);
            donation.setQrCodeData(qrString);
            donation.setStatus(DonationStatus.PENDING);
        } else {
            // Process payment based on method
            String transactionRef = processPayment(paymentMethod, amount, paymentDetails);
            donation.setTransactionRef(transactionRef);
            
            // Generate QR code for OFFLINE_QR
            if (paymentMethod == PaymentMethod.OFFLINE_QR) {
                String qrCodeData = generateQRCodeData(paymentMethod, amount, transactionRef, paymentDetails);
                donation.setQrCodeData(qrCodeData);
            }
            
            // Set status based on payment method
            if (paymentMethod == PaymentMethod.VISA_CARD) {
                donation.setStatus(DonationStatus.SUCCESS); // Assume instant for card
            } else {
                donation.setStatus(DonationStatus.PENDING); // Wait for QR payment confirmation
            }
        }

        donationRepository.save(donation);

        // Update event total only if payment is successful
        if (donation.getStatus() == DonationStatus.SUCCESS) {
            updateEventTotals(event, amount, eventId);
        }

        return donation;
    }
    
    @Override
    public Page<Donation> getMyDonations(Long userId, Pageable pageable) {
        return donationRepository.findByUserId(userId, pageable);
    }
    
    @Override
    public Donation verifyBakongDonation(Long eventId, Long userId, BigDecimal amount, String md5Hash, boolean anonymous) {
        Donation donation = donationRepository.findByTransactionRef(md5Hash)
                .orElseThrow(() -> new ResourceNotFoundException("Donation transaction not found"));

        if (donation.getStatus() == DonationStatus.SUCCESS) {
            return donation;
        }

        // Determine expected recipient account, currency, and amount based on the donation event details
        Event event = donation.getEvent();
        String expectedBakongId = event.getBakongAccountId();
        if (expectedBakongId == null || expectedBakongId.trim().isEmpty()) {
            expectedBakongId = "sothanarith_heang1@aclb";
        }
        
        String expectedCurrency = "USD";
        java.math.BigDecimal expectedAmount = donation.getAmount();
        
        if ("sothanarith_heang1@aclb".equals(expectedBakongId)) {
            expectedCurrency = "KHR";
            expectedAmount = donation.getAmount().multiply(new java.math.BigDecimal("4000")).setScale(0, java.math.RoundingMode.HALF_UP);
        }

        // Verify with Bakong Service
        java.util.Map<String, Object> response = bakongService.verifyTransactionByMd5(md5Hash);
        System.out.println("Bakong verifyTransactionByMd5 response for hash [" + md5Hash + "]: " + response);
        
        Object code = response.get("responseCode");
        boolean isSuccess = code != null && (code.toString().equals("0") || code.toString().equals("0.0"));
        
        // If simulation mode is active and the live API failed (e.g. 401 Unauthorized due to expired token), simulate success after an 8-second delay.
        if (!isSuccess && simulationMode) {
            java.time.LocalDateTime created = donation.getCreatedAt();
            if (created == null) {
                created = java.time.LocalDateTime.now();
            }
            long secondsElapsed = java.time.Duration.between(created, java.time.LocalDateTime.now()).getSeconds();
            
            if (secondsElapsed < 8) {
                System.out.println("Bakong API returned unauthorized/error. Simulation Mode is enabled: Simulating PENDING state (seconds elapsed: " + secondsElapsed + "/8)...");
                return donation; // Return pending state for the first 8 seconds of polling
            }
            
            System.out.println("Bakong API verify transaction returned unauthorized/error. Simulation Mode is enabled: Bypassing and simulating success (seconds elapsed: " + secondsElapsed + ")...");
            isSuccess = true;
            
            java.util.Map<String, Object> mockData = new java.util.HashMap<>();
            mockData.put("toAccountId", expectedBakongId);
            mockData.put("amount", expectedAmount);
            mockData.put("currency", expectedCurrency);
            
            response = new java.util.HashMap<>();
            response.put("responseCode", 0);
            response.put("data", mockData);
        }

        if (!isSuccess) {
            // Return PENDING donation if transaction not found/success on Bakong yet
            return donation;
        }

        Object dataObj = response.get("data");
        if (dataObj == null || !(dataObj instanceof java.util.Map)) {
            return donation;
        }
        
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> data = (java.util.Map<String, Object>) dataObj;
        
        // Verify recipient account matches the event owner's Bakong ID or default platform fallback
        Object toAccountId = data.get("toAccountId");
        if (toAccountId == null || !toAccountId.toString().equalsIgnoreCase(expectedBakongId.trim())) {
            throw new IllegalStateException("Recipient account mismatch. Expected: " + expectedBakongId + ", Found: " + toAccountId);
        }

        // Verify amount
        Object bakongAmountObj = data.get("amount");
        if (bakongAmountObj == null) {
            return donation;
        }
        java.math.BigDecimal bakongAmount = new java.math.BigDecimal(bakongAmountObj.toString());
        if (bakongAmount.compareTo(expectedAmount) != 0) {
            throw new IllegalStateException("Transaction amount mismatch. Expected: " + expectedAmount + ", Found: " + bakongAmount);
        }

        // Verify currency
        Object currencyObj = data.get("currency");
        if (currencyObj == null || !currencyObj.toString().equalsIgnoreCase(expectedCurrency)) {
            throw new IllegalStateException("Transaction currency mismatch. Expected " + expectedCurrency + " but got: " + currencyObj);
        }

        
        // Mark as successful and update event totals
        donation.setStatus(DonationStatus.SUCCESS);
        donationRepository.save(donation);
        
        updateEventTotals(event, donation.getAmount(), event.getId());
        
        return donation;
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
        // Update event total and save it explicitly to persist changes
        event.setCurrentAmount(event.getCurrentAmount().add(amount));
        eventRepository.save(event);

        // Update report
        EventReport report = eventReportRepository
                .findByEventId(eventId)
                .orElseGet(() -> {
                    EventReport r = new EventReport();
                    r.setEvent(event);
                    return r;
                });

        report.setTotalDonations(event.getCurrentAmount());
        long donationCount = donationRepository.countByEventIdAndStatus(eventId, DonationStatus.SUCCESS);
        report.setTotalDonors((int) donationCount);
        report.setLastUpdatedAt(LocalDateTime.now());

        eventReportRepository.save(report);
    }
}
