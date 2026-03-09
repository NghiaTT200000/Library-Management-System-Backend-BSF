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
    public void sendFineCreatedEmail(String toEmail, String fullName, String bookTitle, int daysOverdue, double amount) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(fromAddress);
            mail.setTo(toEmail);
            mail.setSubject("Library Fine Notice – Overdue Book");
            mail.setText(
                "Hi " + fullName + ",\n\n" +
                "You have an overdue fine for the book: \"" + bookTitle + "\".\n\n" +
                "  Days overdue : " + daysOverdue + "\n" +
                "  Amount owed  : $" + String.format("%.2f", amount) + "\n\n" +
                "Please return the book and settle your fine at the library.\n\n" +
                "– Library Team"
            );
            mailSender.send(mail);
            log.info("Fine created email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send fine created email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async("emailExecutor")
    public void sendUnpaidFineReminderEmail(String toEmail, String fullName, String bookTitle, int daysOverdue, double amount) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(fromAddress);
            mail.setTo(toEmail);
            mail.setSubject("Library Reminder – Unpaid Fine");
            mail.setText(
                "Hi " + fullName + ",\n\n" +
                "This is a reminder that you have an outstanding unpaid fine.\n\n" +
                "  Book         : \"" + bookTitle + "\"\n" +
                "  Days overdue : " + daysOverdue + "\n" +
                "  Amount owed  : $" + String.format("%.2f", amount) + "\n\n" +
                "Please visit the library to settle your balance.\n\n" +
                "– Library Team"
            );
            mailSender.send(mail);
            log.info("Unpaid fine reminder sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send unpaid fine reminder to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async("emailExecutor")
    public void sendVerificationEmail(String toEmail, String fullName, String code) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(fromAddress);
            mail.setTo(toEmail);
            mail.setSubject("Verify your Library account");
            mail.setText(
                "Hi " + fullName + ",\n\n" +
                "Thank you for registering. Use the code below to verify your email:\n\n" +
                "  " + code + "\n\n" +
                "This code expires in " + appProperties.getEmailVerification().getExpiryMinutes() + " minutes.\n\n" +
                "– Library Team"
            );

            mailSender.send(mail);
            log.info("Verification email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", toEmail, e.getMessage());
        }
    }
}