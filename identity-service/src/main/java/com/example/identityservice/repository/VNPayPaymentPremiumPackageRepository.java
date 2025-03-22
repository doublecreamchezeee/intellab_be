package com.example.identityservice.repository;

import com.example.identityservice.model.VNPayPaymentPremiumPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VNPayPaymentPremiumPackageRepository extends JpaRepository<VNPayPaymentPremiumPackage, UUID> {
    VNPayPaymentPremiumPackage findByPayment_PaymentId(UUID paymentId);
    List<VNPayPaymentPremiumPackage> findByEndDateBefore(Instant endDate);
    Optional<VNPayPaymentPremiumPackage> findByUserUid(String userUid);
}
