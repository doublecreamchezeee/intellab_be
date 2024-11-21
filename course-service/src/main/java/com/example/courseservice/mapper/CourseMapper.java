package com.example.courseservice.mapper;

import com.example.courseservice.dto.request.CourseCreationRequest;
import com.example.courseservice.dto.response.CourseResponse;
import com.example.courseservice.model.Course;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CourseMapper {
    Course toCourse(CourseCreationRequest request);
    CourseResponse toCourseResponse(Course course);
}
