package com.example.problemservice.repository;

import com.example.problemservice.model.TestCaseOutput;
import com.example.problemservice.model.composite.TestCaseOutputID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TestCaseOutputRepository extends JpaRepository<TestCaseOutput, TestCaseOutputID> {

    Optional<TestCaseOutput> findByToken(UUID token);

    @Query("SELECT t FROM TestCaseOutput t WHERE t.result_status = :status AND t.createdAt <= :timeoutTime")
    List<TestCaseOutput> findTimedOutInQueue(@Param("status") String status, @Param("timeoutTime") Date timeoutTime);

    @Query("SELECT t FROM TestCaseOutput t WHERE t.result_status = :status")
    List<TestCaseOutput> findByStatus(@Param("status") String status);

    @Query("SELECT COUNT(t) FROM TestCaseOutput t WHERE t.result_status = :status")
    long countByStatus(@Param("status") String status);
}
