package com.example.courseservice.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "\"Categories\"")
public class Category {
    @Id
    @Column(name = "Category_id")
    @GeneratedValue
    UUID id;

    @Column(name = "Category_name", nullable = false, columnDefinition = "VARCHAR(100)")
    String name;

    @Column(columnDefinition = "TEXT")
    String description;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parentId")
    Category parent;

    @JsonIgnore
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    List<Category> children;

    String language;
    Boolean isFeatured = false;
    String type;

    @JsonIgnore
    @ManyToMany(mappedBy = "categories")
    List<Question> questions;

    @JsonIgnore
    @ManyToMany(mappedBy = "categories")
    List<Course> courses;

}
