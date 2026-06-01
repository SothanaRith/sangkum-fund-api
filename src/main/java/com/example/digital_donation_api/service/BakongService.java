package com.example.digital_donation_api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class BakongService {

    @Value("${bakong.api.base-url}")
    private String baseUrl;

    @Value("${bakong.token}")
    private String bakongToken;

    private final RestTemplate restTemplate;

    public BakongService() {
        this.restTemplate = new RestTemplate();
    }

    public Map<String, Object> verifyTransactionByMd5(String md5Hash) {
        String url = baseUrl + "/check_transaction_by_md5";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + bakongToken);

        Map<String, String> body = new HashMap<>();
        body.put("md5", md5Hash);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            return response.getBody();
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", -1);
            errorResponse.put("responseMessage", "Failed to verify with Bakong API: " + e.getMessage());
            return errorResponse;
        }
    }
}
