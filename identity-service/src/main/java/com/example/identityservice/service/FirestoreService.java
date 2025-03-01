package com.example.identityservice.service;

import com.example.identityservice.model.User;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import lombok.AllArgsConstructor;
import com.example.identityservice.utility.ParseUUID;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
@AllArgsConstructor
public class FirestoreService {
    @Autowired
    private final Firestore firestore;

    public void createUserById(String id, String firstName, String lastName) throws ExecutionException, InterruptedException {
        DocumentReference documentRef = firestore.collection("users").document(id);

        Map<String, Object> userData = new HashMap<>();
        userData.put("firstName", firstName);
        userData.put("lastName", lastName);

        documentRef.set(userData).get();
    }

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

    public String createUser(String uid, String role) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection("users").document(ParseUUID.normalizeUID(uid).toString());

        Map<String, Object> userData = new HashMap<>();
        userData.put("role", role);
        userData.put("firstName", role);
        userData.put("lastName", uid);
        userData.put("uid", uid);

        WriteResult result = docRef.set(userData).get();
        return "User added with ID: " + ParseUUID.normalizeUID(uid) + " at " + result.getUpdateTime();

    }
}
