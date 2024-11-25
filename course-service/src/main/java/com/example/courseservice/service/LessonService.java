package com.example.courseservice.service;

import com.example.courseservice.dto.request.LessonCreationRequest;
import com.example.courseservice.dto.request.LessonUpdateRequest;
import com.example.courseservice.dto.response.LessonResponse;
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

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class LessonService {
    LessonRepository lessonRepository;
    LessonMapper lessonMapper;
    CourseRepository courseRepository;

    public LessonResponse createLesson(LessonCreationRequest request) {
        if (!courseRepository.existsById(request.getCourseId())) {
            throw new AppException(ErrorCode.COURSE_NOT_EXISTED);
        }

        Lesson lesson = lessonMapper.toLesson(request);
        Course course = courseRepository.findById(request.getCourseId()).get();
        lesson.setCourse(course);

        lesson = lessonRepository.save(lesson);
        return lessonMapper.toLessonResponse(lesson);
    }

    public LessonResponse getLessonById(String lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));
        return lessonMapper.toLessonResponse(lesson);
    }

    public List<LessonResponse> getLessonsByCourseId(String courseId) {
        return lessonRepository.findAllByCourseId(courseId).stream()
                .map(lessonMapper::toLessonResponse).toList();
    }

    public void deleteLesson(String lessonId) {
        lessonRepository.deleteById(lessonId);
    }

    public LessonResponse updateLesson(String lessonId, LessonUpdateRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED));

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        lessonMapper.updateLesson(lesson, request);

        lesson.setCourse(course);

        return lessonMapper.toLessonResponse(lessonRepository.save(lesson));
    }

}
