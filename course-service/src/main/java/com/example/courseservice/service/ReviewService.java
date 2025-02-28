package com.example.courseservice.service;

import com.example.courseservice.client.IdentityClient;
import com.example.courseservice.dto.ApiResponse;
import com.example.courseservice.dto.request.profile.MultipleProfileInformationRequest;
import com.example.courseservice.dto.request.profile.SingleProfileInformationRequest;
import com.example.courseservice.dto.request.review.ReviewCreationRequest;
import com.example.courseservice.dto.request.review.ReviewUpdateRequest;
import com.example.courseservice.dto.response.profile.MultipleProfileInformationResponse;
import com.example.courseservice.dto.response.profile.SingleProfileInformationResponse;
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
import com.example.courseservice.specification.ReviewSpecification;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
    private final IdentityClient identityClient;

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

    public ReviewCreationResponse createReview(ReviewCreationRequest request, UUID userUuid, String userUid) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED));

        if (checkIfUserAlreadyReviewCourse(course.getCourseId(), userUid)) {
            throw new AppException(ErrorCode.USER_ALREADY_REVIEW_COURSE);
        }

        Review review = reviewMapper.toReview(request);
        review.setCourse(course);
        review.setUserUid(userUid);
        review.setUserUuid(userUuid);

        review = reviewRepository.save(review);

        //update course average rating and review count of course
        updateReviewCountAndAverageRating(course, review);

        return reviewMapper.toReviewCreationResponse(review);
    }

    public DetailsReviewResponse getReviewById(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));
        DetailsReviewResponse response = reviewMapper.toDetailsReviewResponse(review);

        // get user info
        try {
            ApiResponse<SingleProfileInformationResponse> userResponse = identityClient
                    .getSingleProfileInformation(
                        new SingleProfileInformationRequest(
                                review.getUserUid()
                        )
                    ).block();

            if (userResponse != null) {
                SingleProfileInformationResponse user = userResponse.getResult();
                response.setDisplayName(user.getDisplayName());
                response.setEmail(user.getEmail());
                response.setPhotoUrl(user.getPhotoUrl());
            }

        } catch (Exception e) {
            log.error("Error when getting user info", e);
        }
        return response;
    }

    public Page<DetailsReviewResponse> getAllReviewsByCourseId(UUID courseId, Integer filteredRating, Pageable pageable) {

        Page<DetailsReviewResponse> response = null;

        if (filteredRating == null ) {
            response =
                    reviewRepository.findAllByCourse_CourseId(courseId, pageable)
                            .map(reviewMapper::toDetailsReviewResponse);
        } else {
            if (filteredRating < 1 || filteredRating > 5) {
                throw new AppException(ErrorCode.INVALID_RATING_VALUE_FILTER);
            }
            Specification<Review> specification = Specification.where(ReviewSpecification
                            .hasCourseId(courseId))
                    .and(ReviewSpecification.hasRating(filteredRating));

            response = reviewRepository.findAll(specification, pageable)
                    .map(reviewMapper::toDetailsReviewResponse);
        }

        // get user info
        try {
            ApiResponse<MultipleProfileInformationResponse> userResponses = identityClient
                    .getMultipleProfileInformation(
                        new MultipleProfileInformationRequest(
                                response.getContent()
                                        .stream()
                                        .map(DetailsReviewResponse::getUserUid)
                                        .toList()
                        )
                    ).block();

            if (userResponses != null) {
                List<SingleProfileInformationResponse> users = userResponses.getResult().getProfiles();

                response.forEach(r -> {
                    SingleProfileInformationResponse user = users.stream()
                            .filter(u ->
                                    u.getUserId()
                                            .equals(
                                                    r.getUserUid()
                                                            .toString()
                                            )
                            )
                            .findFirst()
                            .orElse(null);

                    if (user != null) {
                        r.setDisplayName(user.getDisplayName());
                        r.setEmail(user.getEmail());
                        r.setPhotoUrl(user.getPhotoUrl());
                    }
                });
            }
        } catch (Exception e) {
            log.error("Error when getting user info", e);
        }

        return response;
    }

    public ReviewCreationResponse updateReviewById(UUID reviewId, ReviewUpdateRequest request) {
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

        return reviewMapper.toReviewCreationResponse(review);
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

        if (course.getReviewCount() != null) {
            response.setTotalReviews(course.getReviewCount());
        } else {
            response.setTotalReviews(0);
        }

        if (course.getAverageRating() != null) {
            response.setAverageRating(course.getAverageRating());
        } else {
            response.setAverageRating(0.0);
        }

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

        response.setPercentageFiveStar(((double) fiveStar / course.getReviewCount() * 100));
        response.setPercentageFourStar(((double) fourStar / course.getReviewCount() * 100));
        response.setPercentageThreeStar(((double) threeStar / course.getReviewCount() * 100));
        response.setPercentageTwoStar(((double) twoStar / course.getReviewCount() * 100));
        response.setPercentageOneStar(((double) oneStar / course.getReviewCount() * 100));

        return response;
    }

    public Boolean checkIfUserAlreadyReviewCourse(UUID courseId, String userUid) {
        return reviewRepository.existsByUserUidAndCourse_CourseId(userUid, courseId);
    }


}
