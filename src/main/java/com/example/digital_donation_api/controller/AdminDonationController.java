package com.example.digital_donation_api.controller;

import com.example.digital_donation_api.dto.mapper.DonationMapper;
import com.example.digital_donation_api.dto.response.DonationResponse;
import com.example.digital_donation_api.entity.Donation;
import com.example.digital_donation_api.entity.DonationStatus;
import com.example.digital_donation_api.entity.User;
import com.example.digital_donation_api.repository.DonationRepository;
import com.example.digital_donation_api.repository.EventRepository;
import com.example.digital_donation_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/admin/donations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDonationController {

    private final DonationRepository donationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    /**
     * Get all donations with pagination and filtering
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllDonations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Donation> donationPage;

            if (status != null && !status.isEmpty() && !status.equalsIgnoreCase("all")) {
                try {
                    DonationStatus donationStatus = DonationStatus.valueOf(status.toUpperCase());
                    if (search != null && !search.isEmpty()) {
                        donationPage = donationRepository.findByStatusAndSearch(donationStatus, search.toLowerCase(), pageable);
                    } else {
                        donationPage = donationRepository.findByStatus(donationStatus, pageable);
                    }
                } catch (IllegalArgumentException e) {
                    donationPage = Page.empty(pageable);
                }
            } else if (search != null && !search.isEmpty()) {
                donationPage = donationRepository.findBySearch(search.toLowerCase(), pageable);
            } else {
                donationPage = donationRepository.findAll(pageable);
            }

            List<DonationResponse> responses = donationPage.getContent().stream()
                    .map(DonationMapper::toResponse)
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("content", responses);
            response.put("pageNumber", donationPage.getNumber());
            response.put("pageSize", donationPage.getSize());
            response.put("totalElements", donationPage.getTotalElements());
            response.put("totalPages", donationPage.getTotalPages());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to fetch donations: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get donation by ID with full details
     */
    @GetMapping("/{donationId}")
    public ResponseEntity<Map<String, Object>> getDonationById(@PathVariable Long donationId) {
        try {
            Optional<Donation> donation = donationRepository.findById(donationId);

            if (donation.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Donation not found");
                return ResponseEntity.status(404).body(errorResponse);
            }

            Donation donationEntity = donation.get();
            DonationResponse donationResponse = DonationMapper.toResponse(donationEntity);

            // Add additional details
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", donationResponse);
            response.put("eventTitle", donationEntity.getEvent() != null ? donationEntity.getEvent().getTitle() : "Unknown");
            response.put("donorName", donationEntity.getUser() != null ? donationEntity.getUser().getName() : "Anonymous");
            response.put("donorEmail", donationEntity.getUser() != null ? donationEntity.getUser().getEmail() : "N/A");
            response.put("paymentMethod", donationEntity.getPaymentMethod() != null ? donationEntity.getPaymentMethod().toString() : "N/A");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error fetching donation: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Get donations by status (SUCCESS, PENDING, FAILED)
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Map<String, Object>> getDonationsByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            DonationStatus donationStatus = DonationStatus.valueOf(status.toUpperCase());
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Donation> donationPage = donationRepository.findByStatus(donationStatus, pageable);

            List<DonationResponse> responses = donationPage.getContent().stream()
                    .map(DonationMapper::toResponse)
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("status", status);
            response.put("content", responses);
            response.put("pageNumber", donationPage.getNumber());
            response.put("pageSize", donationPage.getSize());
            response.put("totalElements", donationPage.getTotalElements());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Invalid donation status: " + status);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get pending donations only
     */
    @GetMapping("/pending")
    public ResponseEntity<Map<String, Object>> getPendingDonations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
            Page<Donation> pendingDonations = donationRepository.findByStatus(DonationStatus.PENDING, pageable);

            List<DonationResponse> responses = pendingDonations.getContent().stream()
                    .map(DonationMapper::toResponse)
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("content", responses);
            response.put("count", pendingDonations.getTotalElements());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error fetching pending donations: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Search donations by donor name, event title, or transaction reference
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchDonations(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Donation> donationPage;

            String searchQuery = q != null ? q.toLowerCase() : "";

            if (!searchQuery.isEmpty() && status != null && !status.isEmpty() && !status.equalsIgnoreCase("all")) {
                try {
                    DonationStatus donationStatus = DonationStatus.valueOf(status.toUpperCase());
                    donationPage = donationRepository.findByStatusAndSearch(donationStatus, searchQuery, pageable);
                } catch (IllegalArgumentException e) {
                    donationPage = donationRepository.findBySearch(searchQuery, pageable);
                }
            } else if (!searchQuery.isEmpty()) {
                donationPage = donationRepository.findBySearch(searchQuery, pageable);
            } else if (status != null && !status.isEmpty()) {
                try {
                    DonationStatus donationStatus = DonationStatus.valueOf(status.toUpperCase());
                    donationPage = donationRepository.findByStatus(donationStatus, pageable);
                } catch (IllegalArgumentException e) {
                    donationPage = Page.empty(pageable);
                }
            } else {
                donationPage = donationRepository.findAll(pageable);
            }

            List<DonationResponse> responses = donationPage.getContent().stream()
                    .map(DonationMapper::toResponse)
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("query", q);
            response.put("content", responses);
            response.put("totalResults", donationPage.getTotalElements());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Search failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get donor information and their donation history
     */
    @GetMapping("/donor/{donorId}")
    public ResponseEntity<Map<String, Object>> getDonorInfo(@PathVariable Long donorId) {
        try {
            Optional<User> donor = userRepository.findById(donorId);

            if (donor.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Donor not found");
                return ResponseEntity.status(404).body(errorResponse);
            }

            User donorUser = donor.get();
            List<Donation> donations = donationRepository.findByUserId(donorId);

            BigDecimal totalDonated = donations.stream()
                    .filter(d -> d.getStatus() == DonationStatus.SUCCESS)
                    .map(Donation::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            List<DonationResponse> donationResponses = donations.stream()
                    .map(DonationMapper::toResponse)
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("donorId", donorId);
            response.put("donorName", donorUser.getName());
            response.put("donorEmail", donorUser.getEmail());
            response.put("totalDonated", totalDonated);
            response.put("donationCount", donations.size());
            response.put("successfulDonations", donations.stream().filter(d -> d.getStatus() == DonationStatus.SUCCESS).count());
            response.put("pendingDonations", donations.stream().filter(d -> d.getStatus() == DonationStatus.PENDING).count());
            response.put("failedDonations", donations.stream().filter(d -> d.getStatus() == DonationStatus.FAILED).count());
            response.put("donations", donationResponses);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error fetching donor information: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Get donation statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDonationStats() {
        try {
            long totalDonations = donationRepository.count();
            long successfulDonations = donationRepository.countByStatus(DonationStatus.SUCCESS);
            long pendingDonations = donationRepository.countByStatus(DonationStatus.PENDING);
            long failedDonations = donationRepository.countByStatus(DonationStatus.FAILED);

            BigDecimal totalAmount = donationRepository.sumAmountByStatus(DonationStatus.SUCCESS);
            if (totalAmount == null) totalAmount = BigDecimal.ZERO;

            BigDecimal pendingAmount = donationRepository.sumAmountByStatus(DonationStatus.PENDING);
            if (pendingAmount == null) pendingAmount = BigDecimal.ZERO;

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("totalDonations", totalDonations);
            response.put("successfulDonations", successfulDonations);
            response.put("pendingDonations", pendingDonations);
            response.put("failedDonations", failedDonations);
            response.put("totalAmount", totalAmount);
            response.put("pendingAmount", pendingAmount);
            response.put("successRate", totalDonations > 0 ? (successfulDonations * 100.0 / totalDonations) : 0);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error calculating statistics: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Get recent donations (last N)
     */
    @GetMapping("/recent")
    public ResponseEntity<Map<String, Object>> getRecentDonations(
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            Pageable pageable = PageRequest.of(0, limit, Sort.by("createdAt").descending());
            Page<Donation> recentDonations = donationRepository.findAll(pageable);

            List<DonationResponse> responses = recentDonations.getContent().stream()
                    .map(DonationMapper::toResponse)
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("content", responses);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error fetching recent donations: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
