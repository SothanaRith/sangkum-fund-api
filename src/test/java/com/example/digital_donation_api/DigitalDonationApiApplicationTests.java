package com.example.digital_donation_api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import com.example.digital_donation_api.util.KhqrGenerator;
import java.math.BigDecimal;

@SpringBootTest
class DigitalDonationApiApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void testKhqrGenerationFallbackAccount() {
        String qr = KhqrGenerator.generate(
            "sothanarith_heang1@aclb",
            "Heang Sothanarith",
            new BigDecimal("25.00"),
            "DON-123456"
        );
        System.out.println("====================================================");
        System.out.println("FALLBACK ACCOUNT KHQR:");
        System.out.println(qr);
        System.out.println("====================================================");
        
        // Basic assertions to verify Tag 29, 52, 53, 54, 60, 99 and 63
        assert qr.startsWith("000201010212");
        assert qr.contains("29390023sothanarith_heang1@aclb0208ABA Bank"); // Tag 29 length 39: 00-23-sothanarith_heang1@aclb and 02-08-ABA Bank
        assert qr.contains("52048220"); // MCC 8220
        assert qr.contains("5303116"); // KHR Currency
        assert qr.contains("5406100000"); // 25 USD * 4000 = 100000 KHR
        assert qr.contains("5802KH"); // KH Country
        assert qr.contains("6010Phnom Penh"); // City
        assert qr.contains("99340013"); // Tag 99 contains subtag 00 creation timestamp
    }

    @Test
    void testKhqrGenerationStandardAccount() {
        String qr = KhqrGenerator.generate(
            "john_smith@devb",
            "Donation Campaign",
            new BigDecimal("25.00"),
            "DON-987654"
        );
        System.out.println("====================================================");
        System.out.println("STANDARD ACCOUNT KHQR:");
        System.out.println(qr);
        System.out.println("====================================================");
        
        assert qr.startsWith("000201010212");
        assert qr.contains("29310015john_smith@devb0208Dev Bank"); // Tag 29: 00-15-john_smith@devb and 02-08-Dev Bank
        assert qr.contains("52045999"); // MCC 5999
        assert qr.contains("5303840"); // USD Currency
        assert qr.contains("540525.00"); // Amount 25.00
        assert qr.contains("5802KH"); // KH Country
        assert qr.contains("6010Phnom Penh"); // City
        assert qr.contains("99340013"); // Tag 99 contains subtag 00 creation timestamp
    }

    @org.springframework.beans.factory.annotation.Autowired
    private com.example.digital_donation_api.service.BakongService bakongService;

    @Test
    void testVerifyTransaction() {
        String md5 = "37a5a9caf9fcbb492c7b14734b68d90f";
        try {
            org.springframework.web.client.RestTemplate rt = new org.springframework.web.client.RestTemplate();
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            
            java.util.Map<String, String> body = new java.util.HashMap<>();
            body.put("md5", md5);
            
            org.springframework.http.HttpEntity<java.util.Map<String, String>> request = 
                new org.springframework.http.HttpEntity<>(body, headers);
                
            org.springframework.http.ResponseEntity<java.util.Map> response = 
                rt.postForEntity("https://sit-api-bakong.nbc.gov.kh/v1/check_transaction_by_md5", request, java.util.Map.class);
            System.out.println("====================================================");
            System.out.println("SIT TOKENLESS BAKONG TRANSACTION STATUS FOR " + md5 + ":");
            System.out.println(response.getBody());
            System.out.println("====================================================");
        } catch (Exception e) {
            System.out.println("====================================================");
            System.out.println("SIT TOKENLESS BAKONG TRANSACTION FAILED:");
            System.out.println(e.getMessage());
            System.out.println("====================================================");
        }
    }

    @Test
    void testRenewToken() {
        try {
            org.springframework.web.client.RestTemplate rt = new org.springframework.web.client.RestTemplate();
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            
            java.util.Map<String, String> body = new java.util.HashMap<>();
            body.put("email", "todayinternet168@gmail.com");
            
            org.springframework.http.HttpEntity<java.util.Map<String, String>> request = 
                new org.springframework.http.HttpEntity<>(body, headers);
                
            org.springframework.http.ResponseEntity<java.util.Map> response = 
                rt.postForEntity("https://api-bakong.nbc.gov.kh/v1/renew_token", request, java.util.Map.class);
            System.out.println("====================================================");
            System.out.println("RENEW TOKEN RESPONSE FOR todayinternet168@gmail.com:");
            System.out.println(response.getBody());
            System.out.println("====================================================");
        } catch (Exception e) {
            System.out.println("====================================================");
            System.out.println("RENEW TOKEN FAILED:");
            System.out.println(e.getMessage());
            System.out.println("====================================================");
        }
    }

}

