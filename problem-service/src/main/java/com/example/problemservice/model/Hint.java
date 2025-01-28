package com.example.problemservice.model;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import com.example.problemservice.model.composite.HintID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "\"hints\"")
public class Hint {
    @Id
    @EmbeddedId
    private HintID hintId;

    @Column(columnDefinition = "TEXT")
    String content;

    @MapsId("problemId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id")
    Problem problem;

}
