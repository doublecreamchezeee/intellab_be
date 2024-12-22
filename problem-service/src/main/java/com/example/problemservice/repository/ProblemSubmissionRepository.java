package com.example.problemservice.repository;

import com.example.problemservice.model.Problem;
import com.example.problemservice.model.ProblemSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProblemSubmissionRepository extends JpaRepository<ProblemSubmission, UUID> {
    Optional<ProblemSubmission> findProblemSubmissionByProblemAndUserUid(Problem problem, UUID userUid);
}
//findProblemSubmissionByProblem_Problem_idAndUserUid