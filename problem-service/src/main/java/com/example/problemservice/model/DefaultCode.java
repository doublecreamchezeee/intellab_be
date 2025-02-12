package com.example.problemservice.model;


import com.example.problemservice.model.composite.DefaultCodeId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Comment;
import org.intellij.lang.annotations.Language;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "\"default_code\"")
public class DefaultCode {
    @Id
    @EmbeddedId
    DefaultCodeId defaultCodeId;

    @Column(columnDefinition = "TEXT")
    String code;

    @JsonIgnore
    @MapsId("languageId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_id")
    ProgrammingLanguage language;

    @JsonIgnore
    @MapsId("problemId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id")
    Problem problem;

}
