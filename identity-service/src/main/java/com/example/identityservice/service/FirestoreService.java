package com.example.identityservice.service;

import com.example.identityservice.dto.response.auth.PremiumSubscription;
import com.example.identityservice.dto.request.PaymentRequest;
import com.example.identityservice.dto.response.PaymentResponse;
import com.example.identityservice.exception.AppException;
import com.example.identityservice.exception.ErrorCode;
import com.example.identityservice.model.User;
import com.google.api.core.ApiFuture;
import com.google.api.gax.rpc.NotFoundException;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import lombok.AllArgsConstructor;
import com.example.identityservice.utility.ParseUUID;
import com.google.cloud.firestore.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.bouncycastle.asn1.x500.style.RFC4519Style.uid;

@Service
@AllArgsConstructor
@Slf4j
public class FirestoreService {
    private final Firestore firestore;


    public User getUserByUid(String uid) throws ExecutionException, InterruptedException {
        DocumentSnapshot document = firestore.collection("users").document(ParseUUID.normalizeUID(uid).toString()).get().get();
        if (document.exists()) {
            User user = document.toObject(User.class);
            if (user != null) {
                System.out.println("firestore get role: " + user.getRole());
                return user;
            } else {
                log.error("User object is null for UID: {}", uid);
            }
        }
        log.error("Document not found for userUid: {}", uid);
        return null;
    }

    public List<User> getAllUsers() throws ExecutionException, InterruptedException {
        CollectionReference collectionRef = firestore.collection("users");

        // lấy danh sách users trong collection
        ApiFuture<QuerySnapshot> querySnapshot = collectionRef.get();
        QuerySnapshot query = querySnapshot.get();

        return query.getDocuments().stream()
                .map(document -> document.toObject(User.class)).toList();
    }

    public void updateUserByUid(String uid, String firstName, String lastName) throws ExecutionException, InterruptedException {
        DocumentReference documentRef = firestore.collection("users").document(ParseUUID.normalizeUID(uid).toString());

        Map<String, Object> updates = new HashMap<>();
        if (firstName != null && !firstName.isEmpty()) {
            updates.put("firstName", firstName);
        }
        if (lastName != null && !lastName.isEmpty()) {
            updates.put("lastName", lastName);
        }
        documentRef.update(updates).get(); // Apply the updates

        getUserByUid(ParseUUID.normalizeUID(uid).toString());
    }

    public void updatePublicUserByUid(String uid, Boolean isPublic) throws ExecutionException, InterruptedException {
        DocumentReference documentRef = firestore.collection("users").document(ParseUUID.normalizeUID(uid).toString());

        Map<String, Object> updates = new HashMap<>();
        updates.put("isPublic", isPublic);

        WriteResult result = documentRef.update(updates).get();
    }

    public String createUserByUid(String uid, String role) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection("users").document(ParseUUID.normalizeUID(uid).toString());

        Map<String, Object> userData = new HashMap<>();
        userData.put("role", role.toLowerCase());
        userData.put("firstName", role);
        userData.put("lastName", uid);
        userData.put("uid", uid);

        WriteResult result = docRef.set(userData).get();
        return "User added with ID: " + ParseUUID.normalizeUID(uid) + " at " + result.getUpdateTime();

    }

    public PaymentResponse createPayment(PaymentRequest request) throws ExecutionException, InterruptedException {
        CollectionReference paymentsRef = firestore.collection("payments");

        // Auto-generate an ID for the payment document
        DocumentReference paymentDocRef = paymentsRef.document();

        Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("uid", request.getUid());
        paymentData.put("amount", request.getAmount());
        paymentData.put("currency", request.getCurrency());
        paymentData.put("description", request.getDescription());
        paymentData.put("type", request.getType());
        paymentData.put("timestamp", FieldValue.serverTimestamp());

        WriteResult result = paymentDocRef.set(paymentData).get();
        return getPaymentById(paymentDocRef.getId());
    }

    public PaymentResponse updatePayment(String paymentId, PaymentRequest request) throws ExecutionException, InterruptedException {
        DocumentReference paymentDocRef = firestore.collection("payments").document(paymentId);

        Map<String, Object> updatedPaymentData = new HashMap<>();
        updatedPaymentData.put("amount", request.getAmount());
        updatedPaymentData.put("currency", request.getCurrency());
        updatedPaymentData.put("description", request.getDescription());
        updatedPaymentData.put("type", request.getType());
        updatedPaymentData.put("timestamp", FieldValue.serverTimestamp()); // Update timestamp

        WriteResult result = paymentDocRef.update(updatedPaymentData).get();
        return getPaymentById(paymentId);
    }

    public PaymentResponse getPaymentById(String paymentId) throws ExecutionException, InterruptedException {
        DocumentReference paymentDocRef = firestore.collection("payments").document(paymentId);
        DocumentSnapshot document = paymentDocRef.get().get();

        if (!document.exists()) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        return document.toObject(PaymentResponse.class);
    }

    public List<PaymentResponse> getPaymentsByUserId(String userId) throws ExecutionException, InterruptedException {
        CollectionReference paymentsRef = firestore.collection("payments");
        Query query = paymentsRef.whereEqualTo("uid", userId);

        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<PaymentResponse> payments = new ArrayList<>();

        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            payments.add(document.toObject(PaymentResponse.class));
        }

        return payments;
    }



    public String getRoleByUid(String uid)  {
        try {
            DocumentSnapshot docRef = firestore.collection("users").document(ParseUUID.normalizeUID(uid).toString()).get().get();
            if (!docRef.exists()) {
                log.error("User with UID {} not found in Firestore", uid);
                return null;
            }

            System.out.println("firestore get role: " + docRef.get("role"));
            return (String)docRef.get("role");
        } catch (Exception e) {
            //throw new AppException(ErrorCode.CANNOT_FIND_USER_ROLE_IN_FIRESTORE);
            log.error("Error retrieving user role for UID {}: {}", uid, e.getMessage());
            System.out.println("Error retrieving user role for UID: " + uid + " - " + e.getMessage());
            return null;
        }
    }


    public PremiumSubscription getUserPremiumSubscriptionByUid(String uid){
        try
        {
            DocumentSnapshot sub = firestore.collection("subscriptions").document(ParseUUID.normalizeUID(uid).toString()).get().get();
            if (sub.exists()) {
                PremiumSubscription result = sub.toObject(PremiumSubscription.class);
                DocumentReference plan = sub.get("plan", DocumentReference.class);
                DocumentSnapshot planDoc = plan.get().get();
                String planType = planDoc.get("name", String.class);
                assert result != null;
                result.setPlanType(planType);
                return result;
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        return null;
    }

}
