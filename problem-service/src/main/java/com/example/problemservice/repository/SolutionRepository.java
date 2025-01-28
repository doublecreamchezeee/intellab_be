package com.example.problemservice.repository;

import com.example.problemservice.model.Solution;
import com.example.problemservice.model.composite.SolutionID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SolutionRepository extends JpaRepository<Solution, SolutionID> {
}