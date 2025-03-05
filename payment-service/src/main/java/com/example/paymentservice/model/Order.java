package com.example.paymentservice.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "\"order\"")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Order {
    @Id
    @GeneratedValue
    @Column(name = "order_id")
    UUID orderId;

    @Column(name = "user_uid")
    String userUid;

    @Column(name = "user_uuid")
    UUID userUuid;

    @Column(name = "order_date")
    Instant orderDate;

    @Column(name = "order_status")
    String orderStatus;

    @Column(name = "payment_method")
    String paymentMethod;

    @Column(name = "payment_status")
    String paymentStatus;

    @Column(name = "payment_date")
    Instant paymentDate;

    @Column(name = "payment_amount")
    Float paymentAmount;
}
