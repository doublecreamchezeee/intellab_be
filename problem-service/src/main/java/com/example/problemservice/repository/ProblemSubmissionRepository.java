package com.example.problemservice.repository;

import com.example.problemservice.model.Problem;
import com.example.problemservice.model.ProblemSubmission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProblemSubmissionRepository extends JpaRepository<ProblemSubmission, UUID> {
    Page<ProblemSubmission> findProblemSubmissionByProblemAndUserId(Problem problem, UUID userUid, Pageable pageable);

    List<ProblemSubmission> findProblemSubmissionByProblemAndUserId(Problem problem, UUID userUid);

    List<ProblemSubmission> findTop5ByIsSolvedAndProgrammingLanguageAndProblem_ProblemIdOrderByCreatedAtDesc(
            Boolean isSolved,
            String programmingLanguage,
            UUID problemId
    );

    Page<ProblemSubmission> findProblemSubmissionByUserId(UUID userUid, Pageable pageable);

    void deleteAllByProblem_ProblemId(UUID problemId);

    //    List<ProblemSubmission> findAllByUserIdAndProblem_ProblemId(UUID userUid, UUID problemId);

    Page<ProblemSubmission> findAllByUserIdAndProblem_ProblemId(UUID userUid, UUID problemId, Pageable pageable);

    List<ProblemSubmission> findAllByUserIdAndProblem_ProblemIdAndIsSolved(UUID userUid, UUID problemId, boolean isSolved);


    @Query("SELECT COUNT(DISTINCT ps.problem.problemId) " +
            "FROM ProblemSubmission ps " +
            "WHERE ps.problem.problemLevel = :problemLevel " +
            "AND ps.isSolved = true " +
            "AND ps.userId = :userUid")
    long countSolvedProblemsByLevelAndUser(
            @Param("problemLevel") String problemLevel,
            @Param("userUid") UUID userUid
    );

    @Query("SELECT ps.programmingLanguage, COUNT(distinct ps.problem.problemId) " +
            "FROM ProblemSubmission ps " +
            "WHERE ps.isSolved = true " +
            "AND ps.userId = :userUid " +
            "GROUP BY ps.programmingLanguage " +
            "ORDER BY COUNT(ps) DESC " +
            "LIMIT 3")
    List<Object[]> findTop3LanguagesBySolvedCount(@Param("userUid") UUID userUid);

    @Query("""
            SELECT ps.userId, SUM(ps.scoreAchieved) AS totalScore
            FROM ProblemSubmission ps
            WHERE ps.scoreAchieved = (
                SELECT MAX(sub.scoreAchieved)
                FROM ProblemSubmission sub
                WHERE sub.userId = ps.userId
                AND sub.problem.problemId = ps.problem.problemId
            )
            GROUP BY ps.userId
            ORDER BY totalScore DESC
            """)
    List<Object[]> getLeaderboard();

}