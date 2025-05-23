package com.example.courseservice.mapper;

import com.example.courseservice.dto.request.lesson.LessonCreationRequest;
import com.example.courseservice.dto.request.lesson.LessonUpdateRequest;
import com.example.courseservice.dto.response.course.CourseAndFirstLessonResponse;
import com.example.courseservice.dto.response.lesson.LessonResponse;
import com.example.courseservice.model.Lesson;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface LessonMapper {
    //@Mapping(target = "content", source = "content")


    @Mapping(target = "course", ignore = true)
    Lesson toLesson(LessonUpdateRequest request);

    @Mapping(target = "course", ignore = true)
    @Mapping(target = "lessonId", ignore = true)
    @Mapping(target = "exercise", ignore = true)
    @Mapping(target = "learningLessons", ignore = true)
    void updateLesson(@MappingTarget Lesson lesson, LessonUpdateRequest request);

    @Mapping(target = "courseId", source = "course.courseId")
    @Mapping(target = "quizId", source = "exercise.exerciseId")
    LessonResponse toLessonResponse(Lesson lesson);


    @Mapping(target = "exerciseId", source = "exercise.exerciseId")
    @Mapping(target = "problemId", source = "problemId")
    @Mapping(target = "lessonContent", source = "content")
    @Mapping(target = "lessonDescription", source = "description")
    @Mapping(target = "courseId", ignore = true)
    @Mapping(target = "courseName", ignore = true)
    @Mapping(target = "courseDescription", ignore = true)
    @Mapping(target = "level", ignore = true)
    @Mapping(target = "price", ignore = true)
    @Mapping(target = "unitPrice", ignore = true)
    @Mapping(target = "reviewCount", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "lessonCount", ignore = true)
    CourseAndFirstLessonResponse toCourseAndFirstLessonResponse(Lesson lesson);

    @Mapping(target = "exerciseId", source = "exercise.exerciseId")
    @Mapping(target = "problemId", source = "problemId")
    @Mapping(target = "lessonContent", source = "content")
    @Mapping(target = "lessonDescription", source = "description")
    @Mapping(target = "courseId", ignore = true)
    @Mapping(target = "courseName", ignore = true)
    @Mapping(target = "courseDescription", ignore = true)
    @Mapping(target = "level", ignore = true)
    @Mapping(target = "price", ignore = true)
    @Mapping(target = "unitPrice", ignore = true)
    @Mapping(target = "reviewCount", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "lessonCount", ignore = true)
    CourseAndFirstLessonResponse updateCourseAndFirstLessonResponse(Lesson lesson, @MappingTarget CourseAndFirstLessonResponse courseAndFirstLessonResponse);
}
