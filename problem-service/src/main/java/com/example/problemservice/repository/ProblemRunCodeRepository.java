package com.example.problemservice.repository;

import com.example.problemservice.model.ProblemRunCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProblemRunCodeRepository extends JpaRepository<ProblemRunCode, UUID> {
    Optional<ProblemRunCode> findProblemRunCodeByProblem_ProblemIdAndUserId(UUID problemId, UUID userUid);

    List<ProblemRunCode> findProblemRunCodeByUserIdAndProblem_ProblemId(UUID userUid, UUID problemId);

    void deleteProblemRunCodeByProblem_ProblemId(UUID problemId);
}
