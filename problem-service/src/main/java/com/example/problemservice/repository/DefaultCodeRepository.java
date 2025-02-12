package com.example.problemservice.repository;

import com.example.problemservice.model.DefaultCode;
import com.example.problemservice.model.Problem;
import com.example.problemservice.model.composite.DefaultCodeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DefaultCodeRepository extends JpaRepository<DefaultCode, DefaultCodeId> {
    List<DefaultCode> findByProblem(Problem problem);
}
