package com.example.problemservice.model;


import com.example.problemservice.model.composite.ProblemCategoryID;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "\"problem_category\"")
public class ProblemCategory {
    @Id
    @EmbeddedId
    ProblemCategoryID problemCategoryID;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("problemId")
    @JoinColumn(name = "problem_id")
    Problem problem;
}
