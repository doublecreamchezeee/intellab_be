package com.example.problemservice.repository;

import com.example.problemservice.model.TestCaseOutput;
import com.example.problemservice.model.composite.TestCaseOutputID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TestCaseOutputRepository extends JpaRepository<TestCaseOutput, TestCaseOutputID> {
    Optional<TestCaseOutput> findByToken(UUID token);

    @Query("SELECT COUNT(t) FROM TestCaseOutput t WHERE t.result_status = :status")
    long countByStatus(String status);
}
