package com.example.courseservice.model.compositeKey;




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
public class testCaseOutputID {
    UUID submission_id;
    UUID testcase_id;
}
