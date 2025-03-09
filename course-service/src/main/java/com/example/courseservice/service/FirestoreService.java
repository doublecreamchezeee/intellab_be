package com.example.courseservice.service;

import com.example.courseservice.client.IdentityClient;
import com.example.courseservice.dto.request.profile.SingleProfileInformationRequest;
import com.example.courseservice.model.Firestore.User;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class FirestoreService {

    @Autowired
    private IdentityClient identityClient;
    private Firestore firestore;

    public User getUserById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot document = firestore.collection("users").document(id).get().get();
        if (document.exists()) {
            return document.toObject(User.class);
        }
        return null;
    }

    public String getUsername(UUID userId){
        String userName = null;
        try {
            User user = getUserById(userId.toString());
            try
            {
                userName = Objects.requireNonNull(identityClient.getSingleProfileInformation(
                                new SingleProfileInformationRequest(user.getUid()))
                        .block()).getResult().getDisplayName();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                userName = user.getFirstName() + " " + user.getLastName();
            }
        } catch (ExecutionException | InterruptedException e) {
            System.err.println(e.getMessage());
            return null;
        }
        return userName;
    }
}
