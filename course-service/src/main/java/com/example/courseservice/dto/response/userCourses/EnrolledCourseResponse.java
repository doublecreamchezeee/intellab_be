package com.example.courseservice.dto.response.userCourses;

import com.example.courseservice.model.Course;
import com.example.courseservice.model.compositeKey.EnrollCourse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EnrolledCourseResponse {
    EnrollCourse enrollId;
    Course course;
    Float progressPercent;
    // Done, Learning, Expired
    String status;
    Instant lastAccessedDate;
}
