package com.example.identityservice.model;

import jakarta.persistence.Embeddable;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Embeddable
public class ProblemStat {
    Integer easy;
    Integer medium;
    Integer hard;
    Integer totalProblem;
}
