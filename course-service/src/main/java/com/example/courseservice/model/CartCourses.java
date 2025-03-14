package com.example.courseservice.model;


import com.example.courseservice.model.compositeKey.CartCourseId;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(name ="\"Cart_courses\"")
public class CartCourses {
    @EmbeddedId
    CartCourseId cartCourseId;
}
