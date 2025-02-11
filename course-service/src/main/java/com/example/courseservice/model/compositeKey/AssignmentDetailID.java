package com.example.courseservice.model.compositeKey;


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
public class AssignmentDetailID {
    @Column(name = "assignment_id")
    UUID assignmentId;
    @Column(name = "submit_order")
    Integer submitOrder;
}
