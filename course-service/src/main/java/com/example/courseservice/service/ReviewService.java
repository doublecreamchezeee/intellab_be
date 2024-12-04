package com.example.courseservice.service;

import com.example.courseservice.dto.request.review.ReviewCreationRequest;
import com.example.courseservice.dto.response.rerview.ReviewCreationResponse;
import com.example.courseservice.exception.AppException;
import com.example.courseservice.exception.ErrorCode;
import com.example.courseservice.mapper.ReviewMapper;
import com.example.courseservice.model.Course;
import com.example.courseservice.model.Review;
import com.example.courseservice.repository.CourseRepository;
import com.example.courseservice.repository.ReviewRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ReviewService {
    ReviewRepository reviewRepository;
    ReviewMapper reviewMapper;
    CourseRepository courseRepository;

    public ReviewCreationResponse createReview(ReviewCreationRequest request) {
        Course course = courseRepository.findById(request.getCourseId()).orElseThrow(
                () -> new AppException(ErrorCode.COURSE_NOT_EXISTED)
        );

        Review review = reviewMapper.toReview(request);

        review.setCourse(course);
        review = reviewRepository.save(review);

        return reviewMapper.toReviewCreationResponse(review);
    }
}
