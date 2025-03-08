package com.example.identityservice.service;

import com.example.identityservice.dto.request.PaymentRequest;
import com.example.identityservice.dto.response.PaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    private FirestoreService firestoreService;
    public PaymentResponse createPayment(PaymentRequest request){
        try {
            return firestoreService.createPayment(request);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public PaymentResponse getPaymentById(String id){
        try {
            return firestoreService.getPaymentById(id);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public List<PaymentResponse> getPaymentByUserUid(String userUid){
        try {
            return firestoreService.getPaymentsByUserId(userUid);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
