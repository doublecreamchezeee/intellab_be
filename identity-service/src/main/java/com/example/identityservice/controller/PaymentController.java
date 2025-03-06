package com.example.identityservice.controller;

import com.example.identityservice.dto.request.PaymentRequest;
import com.example.identityservice.dto.response.PaymentResponse;
import com.example.identityservice.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payment")
@Tag(name = "Auth")
public class PaymentController {
    public final PaymentService paymentService;


    @Operation(
            summary = "Create payment"
    )
    @PostMapping(value = "/checkout")
    public ResponseEntity<PaymentResponse> createPayment(@RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.createPayment(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Create payment"
    )
    @PostMapping(value = "/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable String paymentId) {
        PaymentResponse response = paymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Create payment"
    )
    @PostMapping(value = "/checkout")
    public ResponseEntity<List<PaymentResponse>> getPaymentByUserUid(Authentication authentication,) {
        String userUid = (String) authentication.getPrincipal();
        List<PaymentResponse> response = paymentService.getPaymentByUserUid(userUid);
        return ResponseEntity.ok(response);
    }

}
