package com.example.courseservice.repository;

import com.example.courseservice.model.CartCourses;
import com.example.courseservice.model.compositeKey.CartCourseId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartCoursesRepository extends JpaRepository<CartCourses, CartCourseId> {
}
