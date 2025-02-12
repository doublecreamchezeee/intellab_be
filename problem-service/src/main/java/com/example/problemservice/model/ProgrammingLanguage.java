package com.example.problemservice.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "\"programming_language\"")
public class ProgrammingLanguage {
    @Id
    @GeneratedValue
    @Column(name = "programming_language_id")
    Integer id;

    @Column(name = "short_name", columnDefinition = "VARCHAR(20)")
    String shortName;

    @Column(name = "long_name", columnDefinition = "VARCHAR(50)")
    String longName;

    @JsonIgnore
    @OneToMany(mappedBy = "language", fetch = FetchType.LAZY)
    List<DefaultCode> defaultCodes;

}
