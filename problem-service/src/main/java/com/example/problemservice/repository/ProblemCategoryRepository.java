package com.example.problemservice.repository;

import com.example.problemservice.model.ProblemCategory;
import com.example.problemservice.model.composite.ProblemCategoryID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProblemCategoryRepository extends JpaRepository<ProblemCategory, ProblemCategoryID> {
    List<ProblemCategory> findAllByProblemCategoryID_CategoryIdIn(List<Integer> problemCategoryID_categoryId);
}
