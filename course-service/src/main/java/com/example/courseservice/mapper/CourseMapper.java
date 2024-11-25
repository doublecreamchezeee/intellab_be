package com.example.courseservice.mapper;

import com.example.courseservice.dto.request.CourseCreationRequest;
import com.example.courseservice.dto.request.CourseUpdateRequest;
import com.example.courseservice.dto.response.CourseResponse;
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
    void updateCourse(@MappingTarget Course course, CourseUpdateRequest request);

    CourseResponse toCourseResponse(Course course);
}
