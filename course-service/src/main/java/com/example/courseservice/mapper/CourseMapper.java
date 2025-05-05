package com.example.courseservice.mapper;

import com.example.courseservice.dto.request.course.CourseCreationRequest;
import com.example.courseservice.dto.request.course.CourseUpdateRequest;
import com.example.courseservice.dto.request.course.FinalCourseCreationRequest;
import com.example.courseservice.dto.request.course.GeneralCourseCreationRequest;
import com.example.courseservice.dto.response.course.*;
import com.example.courseservice.enums.course.CourseLevel;
import com.example.courseservice.model.Course;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

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
    @Mapping(target = "currentCreationStepDescription", source = "currentCreationStep", qualifiedByName = "mapCourseCreationStepEnums")
    CourseCreationResponse toCourseCreationResponse(Course course);

    @Mapping(target = "categories",source = "categories")
    @Mapping(target = "currentCreationStepDescription", source = "currentCreationStep", qualifiedByName = "mapCourseCreationStepEnums")
    @Mapping(target = "teacherUuid", source = "userId")
    @Mapping(target = "numberOfEnrolledStudents", expression = "java(course.getEnrollCourses() != null ? course.getEnrollCourses().size() : 0)")
    AdminCourseCreationResponse toAdminCourseCreationResponse(Course course);

    @Mapping(target = "categories",source = "categories")
    @Mapping(target = "currentCreationStepDescription", source = "currentCreationStep", qualifiedByName = "mapCourseCreationStepEnums")
    CourseSearchResponse toCourseSearchResponse(Course course);

    @Mapping(target = "categories",source = "categories")
    @Mapping(target = "currentCreationStepDescription", source = "currentCreationStep", qualifiedByName = "mapCourseCreationStepEnums")
    AdminCourseSearchResponse toAdminCourseSearchResponse(Course course);

    @Mapping(target = "categories",source = "categories")
    @Mapping(target = "id", source = "courseId")
    @Mapping(target = "name", source = "courseName")
    @Mapping(target = "rating", source = "averageRating")
    CourseShortResponse toCourseShortResponse(Course course);

    DetailCourseResponse toDetailCourseResponse(Course course, boolean isUserEnrolled);

    CourseResponse toCourseResponse(Course course, int numberOfReviews, float averageRating);

    @Mapping(target = "courseDescription", source = "description")
    @Mapping(target = "lessonCount", expression = "java((course.getLessons() != null) ? course.getLessons().size() : 0)")
    @Mapping(target = "lessonId", ignore = true)
    @Mapping(target = "lessonContent", ignore = true)
    @Mapping(target = "lessonDescription", ignore = true)
    @Mapping(target = "lessonOrder", ignore = true)
    @Mapping(target = "lessonName", ignore = true)
    @Mapping(target = "exerciseId", ignore = true)
    @Mapping(target = "problemId", ignore = true)
    CourseAndFirstLessonResponse toCourseAndFirstLessonResponse(Course course);

    @Mapping(target = "courseId", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "level", expression = "java(request.getLevel().getCode())")
    Course toCourse(GeneralCourseCreationRequest request);

    Course updateCourse(FinalCourseCreationRequest request, @MappingTarget Course course);

    Course updateCourse(GeneralCourseCreationRequest request, @MappingTarget Course course);

    @Named("mapCourseLevelEnums")
    default String mapCourseLevelEnums(CourseLevel level) {
        switch (level) {
            case Beginner:
                return "Beginner";
            case Intermediate:
                return "Intermediate";
            case Advanced:
                return "Advanced";
            default:
                return "unknown";
        }
    }

    @Named("mapCourseCreationStepEnums")
    default String mapCourseCreationStepEnums(int step) {
        switch (step) {
            case 1:
                return "General step";
            case 2:
                return "Add lesson step";
            case 3:
                return "Final step";
            case 4:
                return "Preview step";
            default:
                return "unknown";
        }
    }
}
