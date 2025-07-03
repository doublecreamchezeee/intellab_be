package com.example.courseservice.specification;


import javax.sql.rowset.Predicate;

import com.example.courseservice.model.Category;
import com.example.courseservice.model.Course;
import com.example.courseservice.model.Section;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.UUID;


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
                    criteriaBuilder.lower(root.get("courseName")), "%" + keyword.toLowerCase() + "%");
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
            return criteriaBuilder.greaterThanOrEqualTo(root.get("averageRating"), rating);
        };
    }

    public static Specification<Course> priceSpecification(Boolean price) {
        return (root, query, criteriaBuilder) ->
        {
            if (price == null) {
                return criteriaBuilder.conjunction();
            }

            if (price) {
                return criteriaBuilder.greaterThan(root.get("price"), 0.0);
            }
            else {
                return criteriaBuilder.equal(root.get("price"), 0.0);
            }
        };

    }

    public static Specification<Course> priceRangeSpecification(Float minPrice, Float maxPrice) {
        return (root, query, criteriaBuilder) -> {
            if (minPrice == null && maxPrice == null) {
                return criteriaBuilder.conjunction();
            }

            if (minPrice != null && maxPrice != null) {
                if (minPrice == 0.0 && maxPrice == 0.0) {
                    return criteriaBuilder.equal(root.get("price"), 0.0);
                }
                return criteriaBuilder.between(root.get("price"), minPrice, maxPrice);
            } else if (minPrice != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice);
            } else {
                return criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice);
            }
        };
    }


    public static Specification<Course> isAvailableSpecification(Boolean isAvailable) {
        return (root, query, criteriaBuilder) -> {
            if (isAvailable == null) {
                return null;//criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("isAvailable"), isAvailable);
        };
    }

    public static Specification<Course> sectionSpecification(Integer sectionId) {
        return (root, query, criteriaBuilder) -> {
            if (sectionId == null || sectionId <= 0) {
                return criteriaBuilder.conjunction();
            }

            Join<Course, Section> join = root.join("sections", JoinType.INNER);
            return criteriaBuilder.equal(join.get("id"), sectionId);
        };
    }

    public static Specification<Course> courseNameOrDescriptionSpecification(String name, String description) {
        return (root, query, criteriaBuilder) -> {
            if ((name == null || name.isEmpty()) && (description == null || description.isEmpty())) {
                return criteriaBuilder.conjunction();
            }

            if (name == null || name.isEmpty()) {
                return criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")), "%" + description.toLowerCase() + "%"
                );
            }

            if (description == null || description.isEmpty()) {
                return criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("courseName")), "%" + name.toLowerCase() + "%"
                );
            }

            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("courseName")), "%" + name.toLowerCase() + "%"),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + description.toLowerCase() + "%")
            );
        };
    }

    public static Specification<Course> isCompletedCreationSpecification(Boolean isCompletedCreation) {
        return (root, query, criteriaBuilder) -> {
            if (isCompletedCreation == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("isCompletedCreation"), isCompletedCreation);
        };
    }

    public static Specification<Course> userIdSpecification(UUID userId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("userId"), userId);
        };
    }
}
