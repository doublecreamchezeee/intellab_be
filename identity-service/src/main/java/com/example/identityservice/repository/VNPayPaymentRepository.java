package com.example.identityservice.repository;

import com.example.identityservice.model.VNPayPayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VNPayPaymentRepository
        extends JpaRepository<VNPayPayment, UUID>, JpaSpecificationExecutor<VNPayPayment> {
    Optional<VNPayPayment> findByTransactionReference(String transactionReference);

    Boolean existsByTransactionReference(String transactionReference);

    Page<VNPayPayment> findAllByUserUid(String userUid, Pageable pageable);

    Page<VNPayPayment> findAllByUserUuid(UUID userUuid, Pageable pageable);

    @Query("SELECT COALESCE(SUM(p.paidAmount), 0) FROM VNPayPayment p WHERE p.transactionStatus = 'SUCCESS'")
    Float sumSuccessfulPayments();

    @Query("SELECT COALESCE(SUM(p.paidAmount), 0) FROM VNPayPayment p WHERE p.transactionStatus = 'SUCCESS' AND MONTH(p.createdAt) = :month AND YEAR(p.createdAt) = :year")
    Float sumSuccessfulPaymentsByMonth(@Param("month") int month, @Param("year") int year);

    @Query("""
                SELECT FUNCTION('DATE_TRUNC', :unit, p.createdAt) AS period, SUM(p.amount)
                FROM VNPayPayment p
                WHERE p.status = 'PAID' AND p.createdAt BETWEEN :start AND :end
                GROUP BY period
                ORDER BY period
            """)
    List<Object[]> sumRevenueByRange(
            @Param("unit") String unit,
            @Param("start") Instant start,
            @Param("end") Instant end);
}
