package com.example.courseservice.mapper;

import com.example.courseservice.dto.request.course.CourseCreationRequest;
import com.example.courseservice.dto.request.course.CourseUpdateRequest;
import com.example.courseservice.dto.response.course.*;
import com.example.courseservice.model.Course;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = CategoryMapper.class)
public interface CourseMapper {
    @Mapping(target = "courseId", ignore = true)
    @Mapping(target = "courseName", source = "courseName")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "level", source = "level")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "unitPrice", source = "unitPrice")
    @Mapping(target = "userId", ignore = true)//source = "userUid"
    Course toCourse(CourseCreationRequest request);

    @Mapping(target = "courseId", ignore = true)
    @Mapping(target = "courseName", source = "courseName")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "level", source = "level")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "unitPrice", source = "unitPrice")
    @Mapping(target = "userId", ignore = true)//source = "userUid"
    Course toCourse(CourseUpdateRequest request);

    @Mapping(target = "lessons", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "enrollCourses", ignore = true)
    @Mapping(target = "topic", ignore = true)
    @Mapping(target = "userId", ignore = true)
    void updateCourse(@MappingTarget Course course, CourseUpdateRequest request);

    @Mapping(target = "categories",source = "categories")
    CourseCreationResponse toCourseCreationResponse(Course course);

    @Mapping(target = "categories",source = "categories")
    CourseSearchResponse toCourseSearchResponse(Course course);

    @Mapping(target = "categories",source = "categories")
    @Mapping(target = "id", source = "courseId")
    @Mapping(target = "name", source = "courseName")
    @Mapping(target = "rating", source = "averageRating")
    CourseShortResponse toCourseShortResponse(Course course);

    DetailCourseResponse toDetailCourseResponse(Course course, boolean isUserEnrolled);

    CourseResponse toCourseResponse(Course course, int numberOfReviews, float averageRating);
}
