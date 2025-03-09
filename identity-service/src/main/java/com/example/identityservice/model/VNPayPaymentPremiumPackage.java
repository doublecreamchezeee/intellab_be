package com.example.identityservice.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "\"vnpay_payment_premium_package\"")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class VNPayPaymentPremiumPackage {
    @Id
    @GeneratedValue
    @Column(name = "payment_premium_package_id")
    UUID paymentPremiumPackageId;

    @Column(name = "user_uuid")
    UUID userUuid;

    @Column(name = "user_uid")
    String userUid;

    @JsonIgnore
    @OneToOne(mappedBy = "vnPayPaymentPremiumPackage", fetch = FetchType.LAZY)
    VNPayPayment payment;

    @Column(name = "start_date", nullable = true)
    Instant startDate;

    @Column(name = "package_type")
    String packageType;
}
