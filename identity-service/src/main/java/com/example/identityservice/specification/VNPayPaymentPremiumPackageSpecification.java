package com.example.identityservice.specification;

import com.example.identityservice.model.VNPayPaymentPremiumPackage;
import org.springframework.data.jpa.domain.Specification;

public class VNPayPaymentPremiumPackageSpecification {
    public static Specification<VNPayPaymentPremiumPackage> hasStatus(String status) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"), status);
    }
    public static Specification<VNPayPaymentPremiumPackage> hasUserUid(String userUid) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("userUid"), userUid);
    }
}
