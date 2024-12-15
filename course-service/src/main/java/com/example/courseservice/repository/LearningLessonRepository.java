package com.example.courseservice.repository;

import com.example.courseservice.model.LearningLesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LearningLessonRepository extends JpaRepository<LearningLesson, UUID> {
}
