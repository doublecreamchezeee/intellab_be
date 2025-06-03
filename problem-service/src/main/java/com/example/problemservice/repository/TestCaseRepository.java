package com.example.problemservice.repository;

import com.example.problemservice.model.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TestCaseRepository extends JpaRepository<TestCase, UUID> {
    List<TestCase> findAllByProblem_ProblemId(UUID problemId);

    void deleteAllByProblem_ProblemId(UUID problemId);
}