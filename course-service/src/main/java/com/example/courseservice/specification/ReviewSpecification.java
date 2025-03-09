package com.example.courseservice.specification;

import com.example.courseservice.model.Review;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class ReviewSpecification {
    public static Specification<Review> hasCourseId(UUID courseId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("course").get("courseId"), courseId);
    }

    public static Specification<Review> hasRating(int rating) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("rating"), rating);
    }
}
