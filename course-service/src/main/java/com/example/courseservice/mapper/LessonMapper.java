package com.example.courseservice.mapper;

import com.example.courseservice.dto.request.lesson.LessonCreationRequest;
import com.example.courseservice.dto.request.lesson.LessonUpdateRequest;
import com.example.courseservice.dto.response.lesson.LessonResponse;
import com.example.courseservice.model.Lesson;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface LessonMapper {
    //@Mapping(target = "content", source = "content")

//    @Mapping(target = "course", expression = "java(null)")
//    @Mapping(target = "Lesson_Id", ignore = true)
//    //@Mapping(target = "content", source = "request.content")
//    Lesson toLesson(LessonCreationRequest request);

    @Mapping(target = "course", expression = "java(null)")
    Lesson toLesson(LessonUpdateRequest request);

    @Mapping(target = "course", ignore = true)
    void updateLesson(@MappingTarget Lesson lesson, LessonUpdateRequest request);

    @Mapping(target = "course_id", source = "course.course_id")
    LessonResponse toLessonResponse(Lesson lesson);
}
