package com.example.courseservice.configuration;


import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirestoreConfig {
    @Bean
    public Firestore firestore() throws IOException {
        // Kiểm tra file có tồn tại không
        InputStream serviceAccount = getClass().getClassLoader().getResourceAsStream("firebase-config.json");
        if (serviceAccount == null) {
            throw new IOException("Không tìm thấy file firebase-config.json trong resources!");
        }

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
        }

        return FirestoreClient.getFirestore();
    }
}
