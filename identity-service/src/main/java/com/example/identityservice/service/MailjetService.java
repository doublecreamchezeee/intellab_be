package com.example.identityservice.service;

import com.example.identityservice.exception.AppException;
import com.google.gson.JsonArray;
import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.resource.Emailv31;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class MailjetService {
    @Value("${mailjet.api.key}")
    String apiKey;

    @Value("${mailjet.api.secret}")
    String apiSecret;

    public void sendEmail(String to, String subject, String body) {
        // Implement Mailjet email sending logic here
        // Use apiKey and apiSecret for authentication
        log.info("key: {}, secret: {}", apiKey, apiSecret);
        log.info("Sending email to: {}, Subject: {}", to, subject);

        ClientOptions options = ClientOptions.builder()
                .apiKey(apiKey)
                .apiSecretKey(apiSecret)
                .build();
        // Example of using Mailjet API (pseudo-code)

        MailjetClient client = new MailjetClient(apiKey, apiSecret);

        MailjetRequest request = new MailjetRequest(Emailv31.resource)
                .property(Emailv31.MESSAGES, new JSONArray()
                        .put(new JSONObject()
                                        .put(Emailv31.Message.FROM, new JSONObject()
                                                .put("Email", "tienphamus21120@gmail.com")
                                                .put("Name", "Intellab"))
                                        .put(Emailv31.Message.TO, new JSONArray()
                                                .put(new JSONObject()
                                                        .put("Email", to)
                                                        .put("Name", "recipient")))
                                        .put(Emailv31.Message.CC, new JSONArray()
                                                .put(new JSONObject()
                                                        .put("Email", "copilot@mailjet.com")
                                                        .put("Name", "Copilot")))
                                .put((Emailv31.Message.SUBJECT), subject)
                                .put((Emailv31.Message.HTMLPART), body))
                        );

        try {
            MailjetResponse response = client.post(request);
            log.info("Response status: " + response.getStatus());
            log.info("Data: " + response.getData());

        } catch (MailjetException e) {
            e.printStackTrace();
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }


    }
}
