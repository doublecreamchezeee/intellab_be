package com.example.identityservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendMail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        //message.setFrom("webdevelopmentadvanced2425@gmail.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        //System.out.println("sender: " + mailSender);
        try {
            mailSender.send(message);
            //log.info("Mail sent to {}", to);
        } catch (Exception e) {
            log.info("Failed to send email to {}", to + ", error: " + e.getMessage());
        }
    }

}
