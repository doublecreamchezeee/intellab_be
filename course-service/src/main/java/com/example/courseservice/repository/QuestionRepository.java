package com.example.courseservice.repository;

import com.example.courseservice.model.Exercise;
import com.example.courseservice.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;


@Repository
public interface QuestionRepository extends JpaRepository<Question, UUID> {
    List<Question> findAllByExercises(List<Exercise> exercises);
}
