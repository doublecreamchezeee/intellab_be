package com.example.courseservice.model;


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
@Table(name = "\"sections\"")
public class Section {
    @Id
    @GeneratedValue
    @Column(name = "section_id")
    Integer id;

    String name;

    @ManyToMany(mappedBy = "sections")
    List<Course> courses;

}
