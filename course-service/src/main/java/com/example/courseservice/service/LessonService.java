package com.example.courseservice.service;

import com.example.courseservice.dto.request.lesson.LessonCreationRequest;
import com.example.courseservice.dto.request.lesson.LessonUpdateRequest;
import com.example.courseservice.dto.response.lesson.LessonResponse;
import com.example.courseservice.exception.AppException;
import com.example.courseservice.exception.ErrorCode;
import com.example.courseservice.mapper.LessonMapper;
import com.example.courseservice.model.Course;
import com.example.courseservice.model.Lesson;
import com.example.courseservice.repository.CourseRepository;
import com.example.courseservice.repository.LessonRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class LessonService {
    LessonRepository lessonRepository;
    LessonMapper lessonMapper;
    CourseRepository courseRepository;

//    public LessonResponse createLesson(LessonCreationRequest request) {
//        if (!courseRepository.existsById(request.getCourse_id())) {
//            throw new AppException(ErrorCode.COURSE_NOT_EXISTED);
//        }
//
//        Lesson lesson = lessonMapper.toLesson(request);
//        Course course = courseRepository.findById(request.getCourse_id()).get();
//        lesson.setCourse(course);
//
//        lesson = lessonRepository.save(lesson);
//        return lessonMapper.toLessonResponse(lesson);
//    }

    public LessonResponse getLessonById(String lessonId) {
        Lesson lesson = lessonRepository.findById(UUID.fromString(lessonId))
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));
        return lessonMapper.toLessonResponse(lesson);
    }

    public List<LessonResponse> getLessonsByCourseId(String courseId) {
        return lessonRepository.findAllByCourseId(UUID.fromString(courseId)).stream()
                .map(lessonMapper::toLessonResponse).toList();
    }

    public void deleteLesson(String lessonId) {
        lessonRepository.deleteById(UUID.fromString(lessonId));
    }

    public LessonResponse updateLesson(String lessonId, LessonUpdateRequest request) {
        Course course = courseRepository.findById(request.getCourse_id())
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED));

        Lesson lesson = lessonRepository.findById(UUID.fromString(lessonId))
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        lessonMapper.updateLesson(lesson, request);

        lesson.setCourse(course);

        return lessonMapper.toLessonResponse(lessonRepository.save(lesson));
    }

}
