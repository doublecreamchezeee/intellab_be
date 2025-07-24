package com.example.problemservice.repository.specification;


import com.example.problemservice.model.Problem;
import com.example.problemservice.model.ProblemSubmission;
import com.example.problemservice.model.course.Category;
import jakarta.persistence.criteria.*;
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

    public static Specification<Problem> isPublicFilter(Boolean isPublic) {
        return (root, query, criteriaBuilder) -> {
            if (isPublic == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("isPublished"), isPublic);
        };
    }

    public static Specification<Problem> isCompletedCreationFilter(Boolean isComplete) {
        return (root, query, criteriaBuilder) -> {
            if (isComplete == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("isCompletedCreation"), isComplete);
        };
    }

    public static Specification<Problem> isPublishedFilter(Boolean isPublish) {
        return (root, query, criteriaBuilder) -> {
            if (isPublish == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("isPublished"), isPublish);
        };
    }

    public static Specification<Problem> StatusFilter(Boolean status, UUID userId) {
        /*return (root, query, criteriaBuilder) ->{
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
                Subquery<ProblemSubmission> subquery = query.subquery(ProblemSubmission.class);
                Root<ProblemSubmission> submission = subquery.from(ProblemSubmission.class);
                subquery.select(submission)
                        .where(
                                criteriaBuilder.equal(submission.get("problem"), root),
                                criteriaBuilder.equal(submission.get("isSolved"), status)
                        );

                return criteriaBuilder.not(criteriaBuilder.exists(subquery));
            }
        };*/
        return (root, query, criteriaBuilder) -> {
            // Case 1: status is null - no filtering on solved status for this user
            if (status == null) {
                return null; // Returning null means no predicate is added, effectively no filter
            }

            // Case 2: status is true - find problems the user HAS solved
            if (status) {
                // We need to join to submissions to check for a solved entry by the user.
                // An INNER JOIN is appropriate here because we only care about problems
                // that *have* a matching submission. If a problem has no submissions
                // at all, it won't be considered "solved" by anyone.
                Join<Problem, ProblemSubmission> submissionJoin = root.join("submissions", JoinType.INNER);

                // Predicate: submission must belong to the specified user
                Predicate userPredicate = criteriaBuilder.equal(submissionJoin.get("userId"), userId);
                // Predicate: submission must be marked as solved
                Predicate solvedPredicate = criteriaBuilder.isTrue(submissionJoin.get("isSolved"));

                // Combine conditions: problem has a submission by this user that is solved
                return criteriaBuilder.and(userPredicate, solvedPredicate);
            }
            // Case 3: status is false - find problems the user has NOT solved
            else {
                // We need to find problems for which there is NO submission
                // by this user where isSolved is TRUE.
                // This correctly identifies problems that are "unsolved" by the user,
                // including problems with no submissions from them, or problems
                // where all their submissions are isSolved=false.

                Subquery<UUID> subquery = query.subquery(UUID.class); // Select problemId
                Root<ProblemSubmission> submission = subquery.from(ProblemSubmission.class);

                // Select the problemId from submissions that meet the "solved by this user" criteria
                subquery.select(submission.get("problem").get("problemId")) // Select the problem's ID
                        .where(
                                criteriaBuilder.equal(submission.get("userId"), userId), // By the specific user
                                criteriaBuilder.isTrue(submission.get("isSolved"))     // And is marked as solved
                        );

                // The main query should return problems whose problemId is NOT IN the subquery's results.
                return criteriaBuilder.not(root.get("problemId").in(subquery));
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

    public static Specification<Problem> currentCreationStepGreaterThanOrEqualTo(Integer currentCreationStep) {
        return (root, query, criteriaBuilder) -> {
            if (currentCreationStep == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("currentCreationStep"), currentCreationStep);
        };
    }

    public static Specification<Problem> isCompletedCreationEqualTo(Boolean isCompletedCreation) {
        return (root, query, criteriaBuilder) -> {
            if (isCompletedCreation == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("isCompletedCreation"), isCompletedCreation);
        };
    }

    public static Specification<Problem> authorIdSpecification(UUID userId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("authorId"), userId);
        };
    }
}
