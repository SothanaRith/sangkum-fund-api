package com.example.digital_donation_api.controller;

import com.example.digital_donation_api.entity.Charity;
import com.example.digital_donation_api.service.CharityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/charities")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCharityController {

    private final CharityService charityService;

    @GetMapping
    public ResponseEntity<Page<Charity>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return ResponseEntity.ok(charityService.getAll(PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Charity> getById(@PathVariable Long id) {
        return ResponseEntity.ok(charityService.getById(id));
    }

    @PostMapping("/{id}/verify")
    public ResponseEntity<Map<String, Object>> verify(@PathVariable Long id) {
        charityService.verify(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Charity verified"));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<Map<String, Object>> reject(@PathVariable Long id) {
        charityService.reject(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Charity rejected"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        charityService.delete(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Charity deleted"));
    }
}
