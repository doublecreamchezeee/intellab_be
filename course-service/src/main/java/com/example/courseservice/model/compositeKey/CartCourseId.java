package com.example.courseservice.model.compositeKey;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Embeddable
public class CartCourseId {
    @Column(name = "course_id")
    UUID courseId;
    @Column(name = "cart_id")
    UUID cartId;
}
