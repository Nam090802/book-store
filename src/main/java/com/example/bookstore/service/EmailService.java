package com.example.bookstore.service;

import com.example.bookstore.util.enums.EmailTemplateName;
import jakarta.mail.MessagingException;

public interface EmailService {
    void sendEmail(String to,
                   String username,
                   EmailTemplateName emailTemplateName,
                   String confirmationUrl,
                   String activationCode,
                   String subject) throws MessagingException;
}
