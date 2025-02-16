package com.example.problemservice.repository;

import com.example.problemservice.model.ProgrammingLanguage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProgrammingLanguageRepository extends JpaRepository<ProgrammingLanguage, Integer> {
    Optional<ProgrammingLanguage> findByLongName(String name);
}
