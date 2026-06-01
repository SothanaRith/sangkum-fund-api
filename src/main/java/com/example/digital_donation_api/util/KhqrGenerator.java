package com.example.digital_donation_api.util;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

public class KhqrGenerator {

    private static String formatTLV(String tag, String value) {
        if (value == null || value.isEmpty()) return "";
        int len = value.getBytes(StandardCharsets.UTF_8).length;
        return tag + String.format("%02d", len) + value;
    }

    /**
     * Legacy simple generate method for backwards compatibility.
     * Automatically computes creation and expiration timestamps.
     */
    public static String generate(
            String bakongAccountId,
            String merchantName,
            BigDecimal amount,
            String billNumber
    ) {
        long now = System.currentTimeMillis();
        long expire = now + 3600000; // Default 1 hour expiration
        String acquiringBank = "sothanarith_heang1@aclb".equals(bakongAccountId) ? "ABA Bank" : "Dev Bank";
        String mcc = "sothanarith_heang1@aclb".equals(bakongAccountId) ? "8220" : "5999";
        return generate(
            bakongAccountId,
            merchantName,
            amount,
            billNumber,
            acquiringBank,
            mcc,
            now,
            expire
        );
    }

    /**
     * Common generate method passing default values for optional SDK parameters.
     */
    public static String generate(
            String bakongAccountId,
            String merchantName,
            BigDecimal amount,
            String billNumber,
            String acquiringBank,
            String merchantCategoryCode,
            Long creationTimestamp,
            Long expirationTimestamp
    ) {
        return generate(
            bakongAccountId,
            merchantName,
            amount,
            billNumber,
            acquiringBank,
            merchantCategoryCode,
            creationTimestamp,
            expirationTimestamp,
            null, // upiAccountInformation
            null, // merchantAlternateLanguagePreference
            null, // merchantNameAlternateLanguage
            null, // merchantCityAlternateLanguage
            null, // mobileNumber
            null, // storeLabel
            null, // terminalLabel
            null  // purposeOfTransaction
        );
    }

    /**
     * Master generate method supporting every field in the KHQR SDK Version 2.9 specification.
     */
    public static String generate(
            String bakongAccountId,
            String merchantName,
            BigDecimal amount,
            String billNumber,
            String acquiringBank,
            String merchantCategoryCode,
            Long creationTimestamp,
            Long expirationTimestamp,
            String upiAccountInformation,
            String merchantAlternateLanguagePreference,
            String merchantNameAlternateLanguage,
            String merchantCityAlternateLanguage,
            String mobileNumber,
            String storeLabel,
            String terminalLabel,
            String purposeOfTransaction
    ) {
        StringBuilder qr = new StringBuilder();
        
        // Tag 00: Payload Format Indicator (Mandatory, "01")
        qr.append(formatTLV("00", "01"));
        
        // Tag 01: Point of Initiation Method (11: Static, 12: Dynamic)
        qr.append(formatTLV("01", "12"));

        // Tag 15: UnionPay Merchant Account Information (Optional)
        if (upiAccountInformation != null && !upiAccountInformation.isEmpty()) {
            qr.append(formatTLV("15", upiAccountInformation));
        }

        // Tag 29: Merchant Account Info for Solo Merchant / Individual (Mandatory)
        StringBuilder tag29Val = new StringBuilder();
        tag29Val.append(formatTLV("00", bakongAccountId)); // Sub-tag 00: bakongAccountId
        // Sub-tag 01: accountInformation (Optional)
        // Since we don't have separate accountInformation for the event owner, we leave it out
        // Sub-tag 02: acquiringBank (Optional)
        if (acquiringBank != null && !acquiringBank.isEmpty()) {
            tag29Val.append(formatTLV("02", acquiringBank));
        }
        qr.append(formatTLV("29", tag29Val.toString()));

        // Tag 52: Merchant Category Code (MCC)
        String mcc = (merchantCategoryCode != null && !merchantCategoryCode.isEmpty()) 
                     ? merchantCategoryCode : "5999";
        qr.append(formatTLV("52", mcc));

        // Tag 53: Transaction Currency (116: KHR, 840: USD)
        // Custom business logic for testing account: sothanarith_heang1@aclb uses KHR, others use USD.
        String currencyCode = "840";
        BigDecimal finalAmount = amount;
        if ("sothanarith_heang1@aclb".equals(bakongAccountId)) {
            currencyCode = "116";
            if (amount != null) {
                finalAmount = amount.multiply(new BigDecimal("4000")).setScale(0, java.math.RoundingMode.HALF_UP);
            }
        }
        qr.append(formatTLV("53", currencyCode));

        // Tag 54: Transaction Amount
        if (finalAmount != null) {
            if ("116".equals(currencyCode)) {
                qr.append(formatTLV("54", finalAmount.setScale(0, java.math.RoundingMode.HALF_UP).toString()));
            } else {
                qr.append(formatTLV("54", finalAmount.setScale(2, java.math.RoundingMode.HALF_UP).toString()));
            }
        }

        // Tag 58: Country Code (Mandatory, "KH")
        qr.append(formatTLV("58", "KH"));

        // Tag 59: Merchant Name (Mandatory, Latin/English chars, max 25 chars)
        String nameToUse = merchantName;
        if ("sothanarith_heang1@aclb".equals(bakongAccountId)) {
            nameToUse = "Heang Sothanarith";
        }
        if (nameToUse != null) {
            String cleanName = nameToUse.replaceAll("[^\\x00-\\x7F]", "");
            if (cleanName.length() > 25) cleanName = cleanName.substring(0, 25);
            qr.append(formatTLV("59", cleanName));
        } else {
            qr.append(formatTLV("59", "Donation"));
        }

        // Tag 60: Merchant City (Mandatory, default "Phnom Penh", max 15 chars)
        qr.append(formatTLV("60", "Phnom Penh"));

        // Tag 62: Additional Data Field Template (Optional)
        StringBuilder tag62Val = new StringBuilder();
        if (billNumber != null && !billNumber.isEmpty()) {
            tag62Val.append(formatTLV("01", billNumber));
        }
        if (mobileNumber != null && !mobileNumber.isEmpty()) {
            tag62Val.append(formatTLV("02", mobileNumber));
        }
        if (storeLabel != null && !storeLabel.isEmpty()) {
            tag62Val.append(formatTLV("03", storeLabel));
        }
        if (terminalLabel != null && !terminalLabel.isEmpty()) {
            tag62Val.append(formatTLV("07", terminalLabel));
        }
        if (purposeOfTransaction != null && !purposeOfTransaction.isEmpty()) {
            tag62Val.append(formatTLV("08", purposeOfTransaction));
        }
        if (tag62Val.length() > 0) {
            qr.append(formatTLV("62", tag62Val.toString()));
        }

        // Tag 64: Merchant Alternate Language Template (Optional)
        if (merchantAlternateLanguagePreference != null && !merchantAlternateLanguagePreference.isEmpty()) {
            StringBuilder tag64Val = new StringBuilder();
            tag64Val.append(formatTLV("00", merchantAlternateLanguagePreference));
            if (merchantNameAlternateLanguage != null && !merchantNameAlternateLanguage.isEmpty()) {
                tag64Val.append(formatTLV("01", merchantNameAlternateLanguage));
            }
            if (merchantCityAlternateLanguage != null && !merchantCityAlternateLanguage.isEmpty()) {
                tag64Val.append(formatTLV("02", merchantCityAlternateLanguage));
            }
            qr.append(formatTLV("64", tag64Val.toString()));
        }

        // Tag 99: Timestamp Container (Mandatory for dynamic QR in newer SDK versions)
        long created = (creationTimestamp != null) ? creationTimestamp : System.currentTimeMillis();
        long expired = (expirationTimestamp != null) ? expirationTimestamp : (created + 3600000);
        
        StringBuilder tag99Val = new StringBuilder();
        tag99Val.append(formatTLV("00", String.valueOf(created)));
        tag99Val.append(formatTLV("01", String.valueOf(expired)));
        qr.append(formatTLV("99", tag99Val.toString()));

        // Tag 63: CRC16-CCITT (Mandatory)
        qr.append("6304");
        String crc = calculateCRC16(qr.toString());
        qr.append(crc);

        return qr.toString();
    }

    private static String calculateCRC16(String data) {
        int crc = 0xFFFF; // initial value
        int polynomial = 0x1021; // 0001 0000 0010 0001 (0, 5, 12)

        for (byte b : data.getBytes(StandardCharsets.UTF_8)) {
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b >> (7 - i) & 1) == 1);
                boolean c15 = ((crc >> 15 & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit) {
                    crc ^= polynomial;
                }
            }
        }

        crc &= 0xFFFF;
        return String.format("%04X", crc);
    }
}
