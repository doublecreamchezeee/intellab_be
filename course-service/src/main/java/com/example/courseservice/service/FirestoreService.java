package com.example.courseservice.service;

import com.example.courseservice.model.Firestore.User;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
