package com.example.problemservice.repository;

import com.example.problemservice.model.Problem;
import com.example.problemservice.model.ProblemCategory;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, UUID>, JpaSpecificationExecutor<Problem> {
    @NotNull
    @Override
    Page<Problem> findAll(@NotNull Pageable pageable);
    Page<Problem> findAllByProblemNameContainingIgnoreCase(String searchTerm, Pageable pageable);

    List<Problem> findAllByCategories(List<ProblemCategory> categories);

    long countByProblemLevel(String problemLevel);

    List<Problem> findByProblemLevel(String level);

    @NotNull
    @Override
    Page<Problem> findAll(Specification<Problem> specification, @NotNull Pageable pageable);

}