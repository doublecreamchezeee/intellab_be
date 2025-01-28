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
public class ReportID {
    @Column(name = "report_option_id")
    UUID reportOptionId;
    @Column(name = "destination_id")
    UUID destinationId;
    @Column(name = "owner_id")
    UUID ownerId;
}
