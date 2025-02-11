package com.example.courseservice.mapper;

import com.example.courseservice.dto.request.review.ReviewCreationRequest;
import com.example.courseservice.dto.response.rerview.DetailsReviewResponse;
import com.example.courseservice.dto.response.rerview.ReviewCreationResponse;
import com.example.courseservice.model.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewMapper {
    @Mapping(target = "reviewId", ignore = true)
    @Mapping(target = "rating", source = "rating")
    @Mapping(target = "comment", source = "comment")
   // @Mapping(target = "courseId", source = "courseId")
    @Mapping(target = "userId", ignore = true)
    Review toReview(ReviewCreationRequest request);

    @Mapping(target = "reviewId", source = "reviewId")
    @Mapping(target = "rating", source = "rating")
    @Mapping(target = "comment", source = "comment")
    @Mapping(target = "userUid", source = "userId")
    @Mapping(target = "courseId", source = "course.courseId")
    ReviewCreationResponse toReviewCreationResponse(Review review);

    @Mapping(target = "reviewId", source = "reviewId")
    @Mapping(target = "rating", source = "rating")
    @Mapping(target = "comment", source = "comment")
    @Mapping(target = "userUid", source = "userId")
    @Mapping(target = "courseId", source = "course.courseId")
    DetailsReviewResponse toDetailsReviewResponse(Review review);
}
