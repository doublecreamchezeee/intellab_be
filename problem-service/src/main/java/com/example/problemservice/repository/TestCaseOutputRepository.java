package com.example.problemservice.repository;

import com.example.problemservice.model.TestCase_Output;
import com.example.problemservice.model.composite.testCaseOutputId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TestCaseOutputRepository extends JpaRepository<TestCase_Output, testCaseOutputId> {
    Optional<TestCase_Output> findByToken(UUID token);
}
