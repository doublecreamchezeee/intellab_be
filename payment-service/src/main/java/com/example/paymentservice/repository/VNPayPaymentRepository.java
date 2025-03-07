package com.example.paymentservice.repository;


import com.example.paymentservice.model.VNPayPayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VNPayPaymentRepository extends JpaRepository<VNPayPayment, UUID>, JpaSpecificationExecutor<VNPayPayment> {
    Optional<VNPayPayment> findByTransactionReference(String transactionReference);
    Boolean existsByTransactionReference(String transactionReference);
    Page<VNPayPayment> findAllByUserUid(String userUid, Pageable pageable);
    Page<VNPayPayment> findAllByUserUuid(UUID userUuid, Pageable pageable);
}
