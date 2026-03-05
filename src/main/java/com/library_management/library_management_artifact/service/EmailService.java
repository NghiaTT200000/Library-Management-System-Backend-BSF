package com.library_management.library_management_artifact.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.library_management.library_management_artifact.config.AppProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final AppProperties appProperties;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Async("emailExecutor")
    public void sendVerificationEmail(String toEmail, String fullName, String token) {
        try {
            String verifyLink = appProperties.getBaseUrl() + "/api/auth/verify?token=" + token;

            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(fromAddress);
            mail.setTo(toEmail);
            mail.setSubject("Verify your Library account");
            mail.setText(
                "Hi " + fullName + ",\n\n" +
                "Thank you for registering. Click the link below to verify your email:\n\n" +
                verifyLink + "\n\n" +
                "This link expires in " + appProperties.getEmailVerification().getExpiryMinutes() + " minutes.\n\n" +
                "– Library Team"
            );

            mailSender.send(mail);
            log.info("Verification email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", toEmail, e.getMessage());
        }
    }
}
