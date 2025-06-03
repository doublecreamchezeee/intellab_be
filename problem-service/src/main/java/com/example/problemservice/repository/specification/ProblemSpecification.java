package com.example.problemservice.repository.specification;


import com.example.problemservice.model.Problem;
import com.example.problemservice.model.ProblemSubmission;
import com.example.problemservice.model.course.Category;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.UUID;

public class ProblemSpecification {
    public static Specification<Problem> categoriesFilter(List<Integer> categories) {
        return (root, query, criteriaBuilder) -> {
            if (categories == null || categories.isEmpty()) {
                return null;
            }
                Join<Problem, Category> join = root.join("categories", JoinType.LEFT);

                return join.get("problemCategoryID").get("categoryId").in(categories);
        };
    }

    public static Specification<Problem> levelFilter(String level) {
        return (root, query, criteriaBuilder)
                -> level == null ? null : criteriaBuilder.equal(root.get("problemLevel"), level);
    }

    public static Specification<Problem> NameFilter(String title) {
        return
                (root, query, criteriaBuilder) ->
                        title == null
                                ? null
                                : criteriaBuilder.like(
                                        criteriaBuilder.lower(root.get("problemName")),
                                "%" + title.toLowerCase() + "%");
    }

    public static Specification<Problem> StatusFilter(Boolean status, UUID userId) {
        return (root, query, criteriaBuilder) ->{
            if(status == null) {
                return null;
            }
            if (status) {
                Join<Problem, ProblemSubmission> join = root.join("submissions", JoinType.LEFT);

                var userSubmission = criteriaBuilder.equal(join.get("userId"), userId);

                var isSolved = criteriaBuilder.equal(join.get("isSolved"), status);

                return criteriaBuilder.and(userSubmission, isSolved);
            }
            else {
                Join<Problem, ProblemSubmission> join = root.join("submissions", JoinType.LEFT);

                var userSubmission = criteriaBuilder.equal(join.get("userId"), userId);

                var isSolved = criteriaBuilder.equal(join.get("isSolved"), status);

                var noSubmissions = criteriaBuilder.isNull(join.get("submissionId"));

                var notCorrect = criteriaBuilder.and(isSolved, userSubmission);

                return criteriaBuilder.or(noSubmissions, notCorrect);
            }
        };
    }

    public static Specification<Problem> problemStructureNotNullSpecification(Boolean problemStructureNotNull) {
        return (root, query, criteriaBuilder) -> {
            if (problemStructureNotNull == null) {
                return criteriaBuilder.conjunction();
            }
            if (problemStructureNotNull) {
                return criteriaBuilder.isNotNull(root.get("problemStructure"));
            } else {
                return criteriaBuilder.isNull(root.get("problemStructure"));
            }
        };
    }
}
