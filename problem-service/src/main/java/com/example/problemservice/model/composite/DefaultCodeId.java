package com.example.problemservice.model.composite;


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
public class DefaultCodeId {
    @Column(name = "language_id")
    Integer languageId;

    @Column(name = "problem_id")
    UUID problemId;
}
