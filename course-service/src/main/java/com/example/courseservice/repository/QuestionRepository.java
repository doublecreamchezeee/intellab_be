package com.example.courseservice.repository;

import com.example.courseservice.model.Exercise;
import com.example.courseservice.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Repository
public interface QuestionRepository extends JpaRepository<Question, UUID> {
    List<Question> findByExercise(Exercise exercise);

    List<Question> findByExercise_ExerciseId(UUID quizId);

    @Transactional
    @Modifying
    @Query(value = "UPDATE Question q SET q.exercise.exerciseId = :exerciseId WHERE q.questionId IN :ids")
    void updateExerciseIdForQuestions(@Param("exerciseId") UUID exerciseId, @Param("ids") List<UUID> ids);

    @Transactional
    @Modifying
    @Query(value = "UPDATE Question q SET q.exercise.exerciseId = :exerciseId WHERE q.questionId = :id")
    void updateExerciseIdForQuestion(@Param("exerciseId") UUID exerciseId, @Param("id") UUID id);
}
