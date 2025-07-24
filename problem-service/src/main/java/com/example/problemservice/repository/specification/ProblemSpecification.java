package com.example.problemservice.repository.specification;


import com.example.problemservice.model.Problem;
import com.example.problemservice.model.ProblemSubmission;
import com.example.problemservice.model.course.Category;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
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

//    public static Specification<Problem> StatusFilter(Boolean status, UUID userId) {
//        return (root, query, criteriaBuilder) ->{
//            if(status == null) {
//                return null;
//            }
//
//            if (status) {
//                // lọc các submission của người dùng với problem đã giải quyết
//                if (userId == null) {
//                    return criteriaBuilder.conjunction(); // Trả về điều kiện rỗng nếu userId không được cung cấp
//                }
//
//
//                Join<Problem, ProblemSubmission> join = root.join("submissions", JoinType.LEFT);
//
//                var userSubmission = criteriaBuilder.equal(join.get("userId"), userId);
//
//                var isSolved = criteriaBuilder.equal(join.get("isSolved"), status);
//
//                return criteriaBuilder.and(userSubmission, isSolved);
//            }
//            else {
//                Subquery<ProblemSubmission> subquery = query.subquery(ProblemSubmission.class);
//                Root<ProblemSubmission> submission = subquery.from(ProblemSubmission.class);
//                subquery.select(submission)
//                        .where(
//                                criteriaBuilder.equal(submission.get("problem"), root),
//                                criteriaBuilder.equal(submission.get("isSolved"), status)
//                        );
//
//                return criteriaBuilder.not(criteriaBuilder.exists(subquery));
//            }
//        };
//    }

    public static Specification<Problem> StatusFilter(Boolean status, UUID userId) {
        return (root, query, criteriaBuilder) -> {
            if (status == null || userId == null) {
                return null;
            }

            if (status) {
                // Bài toán mà user này đã solve ít nhất 1 lần
                Subquery<ProblemSubmission> subquery = query.subquery(ProblemSubmission.class);
                Root<ProblemSubmission> subRoot = subquery.from(ProblemSubmission.class);
                subquery.select(subRoot)
                        .where(
                                criteriaBuilder.equal(subRoot.get("problem"), root),
                                criteriaBuilder.equal(subRoot.get("userId"), userId),
                                criteriaBuilder.isTrue(subRoot.get("isSolved"))
                        );
                return criteriaBuilder.exists(subquery);
            } else {
                // Bài toán mà user này **chưa từng solve**
                // Có thể có submission nhưng tất cả đều chưa solve, hoặc không có submission nào
                Subquery<ProblemSubmission> subquery = query.subquery(ProblemSubmission.class);
                Root<ProblemSubmission> subRoot = subquery.from(ProblemSubmission.class);
                subquery.select(subRoot)
                        .where(
                                criteriaBuilder.equal(subRoot.get("problem"), root),
                                criteriaBuilder.equal(subRoot.get("userId"), userId),
                                criteriaBuilder.isTrue(subRoot.get("isSolved"))
                        );
                return criteriaBuilder.not(criteriaBuilder.exists(subquery));
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
