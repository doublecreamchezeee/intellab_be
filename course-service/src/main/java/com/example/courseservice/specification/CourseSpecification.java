package com.example.courseservice.specification;


import javax.sql.rowset.Predicate;

import com.example.courseservice.model.Category;
import com.example.courseservice.model.Course;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;


public class CourseSpecification {
    public static Specification<Course> categoriesSpecification(List<Integer> categories) {
        return (root, query, criteriaBuilder) -> {
            if (categories == null || categories.isEmpty()) {
                return null;
            }
            Join<Course, Category> join = root.join("categories", JoinType.LEFT);

            return join.get("id").in(categories);
        };
    };

    public static Specification<Course> nameSpecification(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (keyword == null || keyword.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")), "%" + keyword.toLowerCase() + "%");
        };
    }

    public static Specification<Course> descriptionSpecification(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (keyword == null || keyword.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")), "%" + keyword.toLowerCase() + "%");
        };
    }

    public static Specification<Course> levelsSpecification(List<String> levels) {
        return (root, query, criteriaBuilder) -> {
            if (levels == null || levels.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            return root.get("level").in(levels);
        };
    }

    public static Specification<Course> ratingSpecification(Float rating) {
        return (root, query, criteriaBuilder) -> {
            if (rating == null || (rating <= 0.0f || rating > 5.0f)) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.greaterThanOrEqualTo(root.get("rating"), rating);
        };
    }

    public static Specification<Course> priceSpecification(Boolean price) {
        return (root, query, criteriaBuilder) ->
        {
            if (price == null) {
                return null;
            }

            if (price) {
                return criteriaBuilder.greaterThan(root.get("price"), 0.0);
            }
            else {
                return null;
            }
        };

    }
}
