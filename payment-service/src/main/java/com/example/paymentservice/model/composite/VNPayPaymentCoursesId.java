package com.example.paymentservice.model.composite;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Embeddable
public class VNPayPaymentCoursesId {
    @Column(name = "course_id")
    UUID courseId;

    @Column(name = "user_uid")
    String userUid;
}
