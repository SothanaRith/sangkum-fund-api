package com.example.digital_donation_api.service.impl;

import com.example.digital_donation_api.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendOtpEmail(String to, String otp, String purpose) {
        // In development mode, just log the OTP instead of sending email
        log.info("==============================================");
        log.info("OTP Email (Development Mode)");
        log.info("To: {}", to);
        log.info("Purpose: {}", purpose);
        log.info("OTP Code: {}", otp);
        log.info("==============================================");
        
        // Uncomment below for production with proper SMTP configuration
        /*
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Your OTP Code - SangKumFund");
            
            String htmlContent = buildOtpEmailContent(otp, purpose);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("OTP email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send OTP email to: {}", to, e);
            throw new RuntimeException("Failed to send OTP email", e);
        }
        */
    }

    @Override
    public void sendWelcomeEmail(String to, String name) {
        // In development mode, just log instead of sending email
        log.info("==============================================");
        log.info("Welcome Email (Development Mode)");
        log.info("To: {}", to);
        log.info("Name: {}", name);
        log.info("==============================================");
        
        // Uncomment below for production with proper SMTP configuration
        /*
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Welcome to SangKumFund!");
            
            String htmlContent = buildWelcomeEmailContent(name);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Welcome email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send welcome email to: {}", to, e);
        }
        */
    }

    private String buildOtpEmailContent(String otp, String purpose) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #f97316 0%%, #f59e0b 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9fafb; padding: 30px; border-radius: 0 0 10px 10px; }
                    .otp-box { background: white; border: 2px dashed #f97316; padding: 20px; text-align: center; margin: 20px 0; border-radius: 8px; }
                    .otp-code { font-size: 32px; font-weight: bold; color: #f97316; letter-spacing: 8px; }
                    .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎯 SangKumFund</h1>
                        <p>One-Time Password Verification</p>
                    </div>
                    <div class="content">
                        <h2>Your OTP Code for %s</h2>
                        <p>Please use the following code to complete your action:</p>
                        <div class="otp-box">
                            <div class="otp-code">%s</div>
                        </div>
                        <p><strong>⚠️ This code will expire in 5 minutes.</strong></p>
                        <p>If you didn't request this code, please ignore this email.</p>
                        <div class="footer">
                            <p>© 2026 SangKumFund. All rights reserved.</p>
                            <p>Making a difference, one donation at a time.</p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(purpose, otp);
    }

    private String buildWelcomeEmailContent(String name) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #f97316 0%%, #f59e0b 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9fafb; padding: 30px; border-radius: 0 0 10px 10px; }
                    .button { background: #f97316; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎉 Welcome to SangKumFund!</h1>
                    </div>
                    <div class="content">
                        <h2>Hello %s!</h2>
                        <p>Thank you for joining SangKumFund - where every donation makes a difference.</p>
                        <p>Your account has been successfully created and verified. You can now:</p>
                        <ul>
                            <li>✅ Create and manage fundraising events</li>
                            <li>✅ Support charitable causes</li>
                            <li>✅ Track your donation impact</li>
                            <li>✅ Connect with a community of givers</li>
                        </ul>
                        <p style="text-align: center;">
                            <a href="http://localhost:3000" class="button">Get Started</a>
                        </p>
                        <div class="footer">
                            <p>© 2026 SangKumFund. All rights reserved.</p>
                            <p>Making a difference, one donation at a time.</p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(name);
    }
}
