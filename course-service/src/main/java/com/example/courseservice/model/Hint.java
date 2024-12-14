package com.example.courseservice.model;


import com.example.courseservice.model.compositeKey.hintID;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "\"hints\"")
public class Hint {
    @EmbeddedId
    hintID hintid;

    @Column(columnDefinition = "TEXT")
    String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("problem_id")
    @JoinColumn(name = "problem_id", nullable = false)
    Problem problem;

}
