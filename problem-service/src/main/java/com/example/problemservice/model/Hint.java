package com.example.problemservice.model;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import com.example.problemservice.model.composite.hintID;

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
    private hintID hintId;

    @Column(columnDefinition = "TEXT")
    String content;

    @MapsId("problem_id")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id")
    Problem problem;

}
