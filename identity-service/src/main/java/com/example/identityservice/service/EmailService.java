package com.example.identityservice.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendMail(String to, String subject, String text) {
        /*SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("Intellab");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);*/

        MimeMessage message = mailSender.createMimeMessage();

        //System.out.println("sender: " + mailSender);
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom("Intellab");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, true);

            mailSender.send(message);
            //log.info("Mail sent to {}", to);
        } catch (Exception e) {
            log.info("Failed to send email to {}", to + ", error: " + e.getMessage());
        }
    }

}
