package com.example.identityservice.repository;

import com.example.identityservice.model.VNPayPaymentPremiumPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VNPayPaymentPremiumPackageRepository extends JpaRepository<VNPayPaymentPremiumPackage, UUID> {
    VNPayPaymentPremiumPackage findByPayment_PaymentId(UUID paymentId);
}
