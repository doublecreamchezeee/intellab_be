package com.example.identityservice.client;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirestoreClient {

    private final Firestore firestore;

    public void saveRole(String userId, String role) {
        try {
            WriteResult result = firestore.collection("users")
                    .document(userId)
                    .set(Map.of("role", role))
                    .get();

            log.info("Role saved for user {} at time: {}", userId, result.getUpdateTime());
        } catch (ExecutionException | InterruptedException e) {
            log.error("Error saving role for user {}: {}", userId, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public String getRole(String userId) {
        try {
            return firestore.collection("users")
                    .document(userId)
                    .get()
                    .get()
                    .getString("role");
        } catch (ExecutionException | InterruptedException e) {
            log.error("Error retrieving role for user {}: {}", userId, e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
