package com.example.identityservice.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "\"vnpay_payment\"")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class VNPayPayment {
    @Id
    @GeneratedValue
    @Column(name = "payment_id")
    UUID paymentId;

    @Column(name = "user_uid")
    String userUid;

    @Column(name = "user_uuid")
    UUID userUuid;

    @Column(name = "transaction_status")
    String transactionStatus;

    @Column(name = "response_code")
    String responseCode;

    @Column(name = "total_payment_amount")
    Float totalPaymentAmount;

    @Column(name = "currency")
    String currency;

    @Column(name = "paid_amount")
    Float paidAmount;

    @Column(name = "bank_code")
    String bankCode;

    @Column(name = "transaction_reference")
    String transactionReference;

    @CreationTimestamp
    @Column(name = "created_at")
    Instant createdAt;

    @Column(name = "received_at", columnDefinition = "TIMESTAMP WITH TIME ZONE", nullable = true)
    Instant receivedAt;

    @Column(name = "bank_transaction_no")
    String bankTransactionNo;

    @Column(name = "transaction_no")
    String transactionNo;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    List<VNPayPaymentCourses> paymentCourses = new ArrayList<>();

}
