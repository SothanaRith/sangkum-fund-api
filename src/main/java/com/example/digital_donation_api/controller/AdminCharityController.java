package com.example.digital_donation_api.controller;

import com.example.digital_donation_api.entity.Charity;
import com.example.digital_donation_api.entity.CharityStatus;
import com.example.digital_donation_api.repository.CharityRepository;
import com.example.digital_donation_api.service.CharityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/charities")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCharityController {

    private final CharityRepository charityRepository;
    private final CharityService charityService;

    @GetMapping
    public ResponseEntity<List<Charity>> getAll() {
        return ResponseEntity.ok(charityRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Charity> getById(@PathVariable Long id) {
        return charityRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/verify")
    public ResponseEntity<Map<String, Object>> verify(@PathVariable Long id) {
        charityService.verify(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Charity verified"));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<Map<String, Object>> reject(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Charity charity = charityRepository.findById(id).orElseThrow(() -> new RuntimeException("Charity not found"));
        charity.setStatus(CharityStatus.REJECTED);
        // Save the reason if there is a field for it in Charity, else just update status.
        charityRepository.save(charity);
        return ResponseEntity.ok(Map.of("success", true, "message", "Charity rejected"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        charityRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Charity deleted"));
    }
}
