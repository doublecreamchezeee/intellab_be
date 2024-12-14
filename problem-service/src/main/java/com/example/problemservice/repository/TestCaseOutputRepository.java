package com.example.problemservice.repository;

import com.example.problemservice.model.TestCase_Output;
import com.example.problemservice.model.composite.testCaseOutputId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestCaseOutputRepository extends JpaRepository<TestCase_Output, testCaseOutputId> {
}
