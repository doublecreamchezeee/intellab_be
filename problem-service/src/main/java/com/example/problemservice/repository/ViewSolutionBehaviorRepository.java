package com.example.problemservice.repository;

import com.example.problemservice.model.ViewSolutionBehavior;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ViewSolutionBehaviorRepository extends JpaRepository<ViewSolutionBehavior, Integer> {
    ViewSolutionBehavior findByProblemIdAndUserId(UUID problemId, UUID userId);
}
