package com.example.identityservice.model;


import com.example.identityservice.model.composite.VNPayPaymentCoursesId;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "\"vnpay_payment_courses\"")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class VNPayPaymentCourses {
    @EmbeddedId
    VNPayPaymentCoursesId id;

    @JsonBackReference
    @JoinColumn(name = "payment_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    VNPayPayment payment;
}
