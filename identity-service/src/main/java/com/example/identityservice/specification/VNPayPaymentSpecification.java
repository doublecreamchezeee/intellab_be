package com.example.identityservice.specification;

import com.example.identityservice.model.VNPayPayment;
import org.springframework.data.jpa.domain.Specification;

public class VNPayPaymentSpecification {
    public static Specification<VNPayPayment> hasPaymentFor(String paymentFor) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("paymentFor"), paymentFor);
    }

    public static Specification<VNPayPayment> hasUserUid(String userUid) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("userUid"), userUid);
    }
}
