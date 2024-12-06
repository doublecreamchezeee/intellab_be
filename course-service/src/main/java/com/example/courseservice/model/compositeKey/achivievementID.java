package com.example.courseservice.model.compositeKey;



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
public class achivievementID {
    UUID medal_id;
    @JoinColumn(name = "user_id")
    UUID user_id;
}
