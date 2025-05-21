package com.example.courseservice.repository;

import com.example.courseservice.model.CourseSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CourseSummaryRepository extends JpaRepository<CourseSummary, UUID> {
}
