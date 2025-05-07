package com.example.identityservice.repository;

import com.example.identityservice.model.VNPayPaymentPremiumPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VNPayPaymentPremiumPackageRepository
        extends JpaRepository<VNPayPaymentPremiumPackage, UUID>, JpaSpecificationExecutor<VNPayPaymentPremiumPackage> {
    VNPayPaymentPremiumPackage findByPayment_PaymentId(UUID paymentId);

    List<VNPayPaymentPremiumPackage> findByEndDateBefore(Instant endDate);

    List<VNPayPaymentPremiumPackage> findAllByEndDateBeforeAndStatus(Instant endDate, String status);

    Optional<VNPayPaymentPremiumPackage> findByUserUid(String userUid);
    // TODO: check if this is correct
    // Optional<VNPayPaymentPremiumPackage>
    // findFirstByUserUidOrderByEndDateDesc(String userUid);

    Optional<VNPayPaymentPremiumPackage> findFirstByUserUidAndStatusOrderByEndDateDesc(String userUid, String status);

    List<VNPayPaymentPremiumPackage> findAllByUserUidAndStatus(String userUid, String status);

    @Query("SELECT COUNT(p) FROM VNPayPaymentPremiumPackage p WHERE p.status = 'ACTIVE'")
    int countSuccessfulSubscriptions();

    @Query("SELECT COUNT(p) FROM VNPayPaymentPremiumPackage p WHERE p.status = 'ACTIVE' AND MONTH(p.startDate) = :month AND YEAR(p.startDate) = :year")
    int countNewSubscriptionsByMonth(@Param("month") int month, @Param("year") int year);

    @Query("""
            SELECT FUNCTION('DATE_TRUNC', :unit, p.startDate) AS period, COUNT(p)
            FROM VNPayPaymentPremiumPackage p
            WHERE p.status = 'ACTIVE' AND p.startDate BETWEEN :start AND :end
            GROUP BY period
            """)
    List<Object[]> countSubscriptionsByRange(
            @Param("unit") String unit,
            @Param("start") Instant start,
            @Param("end") Instant end);
}
