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
    Optional<List<ProblemSubmission>> findProblemSubmissionByProblemAndUserId(Problem problem, UUID userUid);

    List<ProblemSubmission> findAllByUserIdAndProblem_ProblemId(UUID userUid, UUID problemId);

    @Query("SELECT COUNT(DISTINCT ps.problem.problemId) " +
            "FROM ProblemSubmission ps " +
            "WHERE ps.problem.problemLevel = :problemLevel " +
            "AND ps.isSolved = true " +
            "AND ps.userId = :userUid")
    long countSolvedProblemsByLevelAndUser(
            @Param("problemLevel") String problemLevel,
            @Param("userUid") UUID userUid
    );


}