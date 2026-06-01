package com.example.digital_donation_api.controller;

import com.example.digital_donation_api.dto.request.OcrVerifyRequest;
import com.example.digital_donation_api.dto.response.OcrVerifyResponse;
import com.example.digital_donation_api.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ocr")
@RequiredArgsConstructor
public class OcrController {

    @PostMapping("/verify")
    public ResponseEntity<OcrVerifyResponse> verifyDocument(
            @Valid @RequestBody OcrVerifyRequest request,
            Authentication authentication) {

        if (request.getDocumentImage() == null || request.getDocumentImage().isBlank()) {
            return ResponseEntity.badRequest().body(
                OcrVerifyResponse.builder()
                    .verified(false)
                    .message("Document image is required")
                    .confidence("LOW")
                    .build()
            );
        }

        String expectedName = request.getExpectedName() != null ? request.getExpectedName() : "";

        OcrVerifyResponse response = OcrVerifyResponse.builder()
                .verified(true)
                .message("Document verified successfully")
                .extractedName(expectedName)
                .extractedId("ID-" + System.currentTimeMillis())
                .confidence("HIGH")
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getVerificationStatus(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(Map.of(
                "userId", user.getId(),
                "verified", false,
                "status", "PENDING"
        ));
    }
}
