package com.example.problemservice.repository;

import com.example.problemservice.model.Solution;
import com.example.problemservice.model.composite.SolutionID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SolutionRepository extends JpaRepository<Solution, SolutionID> {
    List<Solution> findAllByIdProblemId(UUID problemId); // id.problemId
    Optional<Solution> findByIdProblemId(UUID problemId);

    void deleteByIdProblemId(UUID problemId);
}