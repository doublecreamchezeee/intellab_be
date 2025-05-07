package com.example.identityservice.repository;

import com.example.identityservice.model.VNPayPaymentCourses;
import com.example.identityservice.model.composite.VNPayPaymentCoursesId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VNPayPaymentCoursesRepository extends JpaRepository<VNPayPaymentCourses, VNPayPaymentCoursesId> {
    Optional<VNPayPaymentCourses> findByPayment_paymentId(UUID payment_paymentId);

    @Query("SELECT COUNT(c) FROM VNPayPaymentCourses c WHERE c.payment.transactionStatus = 'SUCCESS'")
    int countSuccessfulCoursePurchases();

    @Query("SELECT COUNT(c) FROM VNPayPaymentCourses c WHERE c.payment.transactionStatus = 'SUCCESS' AND MONTH(c.payment.createdAt) = :month AND YEAR(c.payment.createdAt) = :year")
    int countSuccessfulCoursePurchasesByMonth(@Param("month") int month, @Param("year") int year);
}
