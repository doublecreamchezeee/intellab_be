package com.example.identityservice.service;

import com.example.identityservice.client.ZeptoMailClient;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class EmailService {
    final ZeptoMailClient zeptoMailClient;

    @Value("${zeptomail.auth-token}")
    String zeptoMailAuthToken;
    //MailjetService mailjetService;

    //private final JavaMailSender mailSender;


/*
    @Async
    public void sendMail(String to, String subject, String text) {
        */
/*SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("Intellab");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);*//*


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
*/

    @Async
    public void sendMail(String to, String subject, String text) {
        //mailjetService.sendEmail(to, subject, text);
        zeptoMailClient.sendEmail(
                com.example.identityservice.dto.request.zeptomail.SendingEmailRequest.builder()
                        .from(new com.example.identityservice.dto.request.zeptomail.SendingEmailRequest.From("noreply@intellab.org"))
                        .to(List.of(new com.example.identityservice.dto.request.zeptomail.SendingEmailRequest.To(
                                new com.example.identityservice.dto.request.zeptomail.SendingEmailRequest.EmailAddress(to, "Recipient"))))
                        .subject(subject)
                        .htmlbody(text)
                        .build(),
                zeptoMailAuthToken
        );
    }
}
