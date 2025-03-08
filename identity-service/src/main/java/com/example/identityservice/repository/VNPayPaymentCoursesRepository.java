package com.example.identityservice.repository;

import com.example.identityservice.model.VNPayPaymentCourses;
import com.example.identityservice.model.composite.VNPayPaymentCoursesId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VNPayPaymentCoursesRepository extends JpaRepository<VNPayPaymentCourses, VNPayPaymentCoursesId> {
    Optional<VNPayPaymentCourses> findByPayment_paymentId(UUID payment_paymentId);
}
