package com.example.problemservice.repository;

import com.example.problemservice.model.Problem;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, UUID> {
    @NotNull
    @Override
    Page<Problem> findAll(@NotNull Pageable pageable);

    Page<Problem> findAllByProblemNameContainingIgnoreCase(String searchTerm, Pageable pageable);
}
