package com.example.courseservice.mapper;

import com.example.courseservice.dto.request.course.CourseCreationRequest;
import com.example.courseservice.dto.request.course.CourseUpdateRequest;
import com.example.courseservice.dto.response.course.CourseCreationResponse;
import com.example.courseservice.dto.response.course.CourseResponse;
import com.example.courseservice.dto.response.course.DetailCourseResponse;
import com.example.courseservice.model.Course;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CourseMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "level", source = "level")
    Course toCourse(CourseCreationRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "level", source = "level")
    Course toCourse(CourseUpdateRequest request);

    @Mapping(target = "lessons", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    void updateCourse(@MappingTarget Course course, CourseUpdateRequest request);

    CourseCreationResponse toCourseCreationResponse(Course course);

    DetailCourseResponse toDetailCourseResponse(Course course, boolean isUserEnrolled);

    CourseResponse toCourseResponse(Course course, int numberOfReviews, float averageRating);
}
