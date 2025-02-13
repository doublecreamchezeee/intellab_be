package com.example.courseservice.service;

import com.example.courseservice.dto.request.review.ReviewCreationRequest;
import com.example.courseservice.dto.request.review.ReviewUpdateRequest;
import com.example.courseservice.dto.response.rerview.CourseReviewsStatisticsResponse;
import com.example.courseservice.dto.response.rerview.DetailsReviewResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ReviewService {
    ReviewRepository reviewRepository;
    CourseRepository courseRepository;
    ReviewMapper reviewMapper;

    private void updateReviewCountAndAverageRating(Course course, Review review) {
        //update course average rating and review count of course
        Integer reviewCount = course.getReviewCount();
        if (reviewCount == null) {
            reviewCount = 0;
        }

        Double averageRating = course.getAverageRating();
        if (averageRating == null) {
            averageRating = 0.0;
        }

        course.setReviewCount(reviewCount + 1);
        course.setAverageRating(
                (averageRating * (reviewCount) //old average rating
                        + review.getRating()) //add new rating
                        / course.getReviewCount()
        );

        courseRepository.save(course);
    }
    public ReviewCreationResponse createReview(ReviewCreationRequest request, UUID userUid) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED));

        Review review = reviewMapper.toReview(request);
        review.setCourse(course);
        review.setUserId(userUid);

        review = reviewRepository.save(review);

        //update course average rating and review count of course
        updateReviewCountAndAverageRating(course, review);

        return reviewMapper.toReviewCreationResponse(review);
    }

    public DetailsReviewResponse getReviewById(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));
        return reviewMapper.toDetailsReviewResponse(review);
    }

    public Page<DetailsReviewResponse> getAllReviewsByCourseId(UUID courseId, Pageable pageable) {
        return reviewRepository.findAllByCourse_CourseId(courseId, pageable)
                .map(reviewMapper::toDetailsReviewResponse);
    }

    public DetailsReviewResponse updateReviewById(UUID reviewId, ReviewUpdateRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        Course course = review.getCourse();

        Integer reviewCount = course.getReviewCount();
        if (reviewCount == null) {
            reviewCount = 0;
        }

        Double averageRating = course.getAverageRating();
        if (averageRating == null) {
            averageRating = 0.0;
        }

        course.setAverageRating(
                (averageRating * (reviewCount) //old average rating
                        - review.getRating() + request.getRating() ) //add new rating and subtract old rating
                        / course.getReviewCount()
        );

        courseRepository.save(course);

        review.setRating(request.getRating());
        review.setComment(request.getComment());

        review = reviewRepository.save(review);

        return reviewMapper.toDetailsReviewResponse(review);
    }

    public void deleteReviewById(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        Course course = review.getCourse();

        reviewRepository.delete(review);

        //update course average rating and review count of course
        Integer reviewCount = course.getReviewCount();
        if (reviewCount == null) {
            reviewCount = 0;
        }

        Double averageRating = course.getAverageRating();
        if (averageRating == null) {
            averageRating = 0.0;
        }

        course.setReviewCount(reviewCount - 1);
        course.setAverageRating(
                (averageRating * (reviewCount) //old average rating
                        - review.getRating()) //remove rating
                        / course.getReviewCount()
        );

        courseRepository.save(course);
    }

    public CourseReviewsStatisticsResponse getCourseReviewsStatisticsByCourseId(UUID courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED));

        List<Review> review =  reviewRepository.findAllByCourse_CourseId(courseId);

        CourseReviewsStatisticsResponse response = new CourseReviewsStatisticsResponse();

        response.setTotalReviews(course.getReviewCount());
        response.setAverageRating(course.getAverageRating());
        response.setCourseId(courseId);

        int fiveStar = 0;
        int fourStar = 0;
        int threeStar = 0;
        int twoStar = 0;
        int oneStar = 0;

        for (Review r : review) {
            if (r.getRating() == 5) {
                fiveStar++;
            } else if (r.getRating() == 4) {
                fourStar++;
            } else if (r.getRating() == 3) {
                threeStar++;
            } else if (r.getRating() == 2) {
                twoStar++;
            } else if (r.getRating() == 1) {
                oneStar++;
            }
        }

        response.setFiveStar(fiveStar);
        response.setFourStar(fourStar);
        response.setThreeStar(threeStar);
        response.setTwoStar(twoStar);
        response.setOneStar(oneStar);

        return response;
    }


}
