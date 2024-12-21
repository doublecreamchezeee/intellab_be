package com.example.courseservice.repository.custom;

import com.example.courseservice.dto.response.course.DetailCourseResponse;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DetailsCourseRepositoryCustom {
    Optional<DetailCourseResponse> getDetailsCourse(UUID courseId, UUID userId);
}
