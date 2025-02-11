package com.example.problemservice.repository;

import com.example.problemservice.model.ProblemRunCode;
import com.example.problemservice.model.TestCaseOutput;
import com.example.problemservice.model.TestCaseRunCodeOutput;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TestCaseRunCodeOutputRepository extends JpaRepository<TestCaseRunCodeOutput, UUID> {
    Optional<TestCaseRunCodeOutput> findTestCaseRunCodeOutputByToken(UUID token);
    Optional<TestCaseRunCodeOutput> findByToken(UUID token);
    void deleteAllByRunCode(ProblemRunCode runCode);
}
