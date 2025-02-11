package com.example.problemservice.model.composite;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.UUID;



@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Embeddable
public class ProblemCategoryID implements Serializable {
    @JoinColumn(name = "category_id",nullable = false)
    Integer categoryId;

    @Column(name = "problem_id")
    UUID problemId;
}
