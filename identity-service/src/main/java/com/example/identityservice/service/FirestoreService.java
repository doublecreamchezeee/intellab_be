package com.example.identityservice.service;

import com.example.identityservice.model.User;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class FirestoreService {

    @Autowired
    private Firestore firestore;

    public User getUserById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot document = firestore.collection("users").document(id).get().get();
        if (document.exists()) {
            return document.toObject(User.class);
        }
        return null;
    }

    public User updateUserById(String id, String firstName, String lastName) throws ExecutionException, InterruptedException {
        DocumentReference documentRef = firestore.collection("users").document(id);

        Map<String, Object> updates = new HashMap<>();
        if (firstName != null && !firstName.isEmpty()) {
            updates.put("firstName", firstName);
        }
        if (lastName != null && !lastName.isEmpty()) {
            updates.put("lastName", lastName);
        }
        documentRef.update(updates).get(); // Apply the updates

        return getUserById(id); // Fetch and return the updated user
    }
}
