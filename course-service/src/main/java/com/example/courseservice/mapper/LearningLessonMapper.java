package com.example.courseservice.mapper;

import com.example.courseservice.dto.request.learningLesson.LearningLessonCreationRequest;
import com.example.courseservice.dto.response.learningLesson.LearningLessonResponse;
import com.example.courseservice.dto.response.learningLesson.LessonUserResponse;
import com.example.courseservice.model.LearningLesson;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LearningLessonMapper {
      LearningLesson toLearningLesson(LearningLessonCreationRequest request);

      @Mapping(target = "lessonId", source = "lesson.lessonId")
      LearningLessonResponse toLearningLessonResponse(LearningLesson learningLesson);

      @Mapping(target = "lesson", source = "lesson")
      LessonUserResponse toLessonUserResponse(LearningLesson learningLesson);
}
