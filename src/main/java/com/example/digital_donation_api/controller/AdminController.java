package com.example.digital_donation_api.controller;

import com.example.digital_donation_api.dto.mapper.UserMapper;
import com.example.digital_donation_api.dto.response.UserResponse;
import com.example.digital_donation_api.entity.User;
import com.example.digital_donation_api.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/charities/{charityId}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> verifyCharity(@PathVariable Long charityId) {
        adminService.verifyCharity(charityId);
        return ResponseEntity.ok("Charity verified successfully");
    }
}
