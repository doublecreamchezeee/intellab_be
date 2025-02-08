package com.example.problemservice.repository;

import com.example.problemservice.model.Problem;
import com.example.problemservice.model.ProblemSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProblemSubmissionRepository extends JpaRepository<ProblemSubmission, UUID> {
    Optional<List<ProblemSubmission>> findProblemSubmissionByProblemAndUserUid(Problem problem, UUID userUid);

    List<ProblemSubmission> findProblemSubmissionByUserUidAndProblem_ProblemId(UUID userUid, UUID problemId);

    @Query("SELECT COUNT(ps) " +
            "FROM ProblemSubmission ps " +
            "WHERE ps.problem.problemLevel = :problemLevel " +
            "AND ps.isSolved = TRUE " +
            "AND ps.userUid = :userId")
    long countSolvedProblemsByLevelAndUser(@Param("problemLevel") String problemLevel,
                                           @Param("userId") UUID userId);

}