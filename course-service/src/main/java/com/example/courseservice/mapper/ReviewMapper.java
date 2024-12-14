package com.example.courseservice.mapper;

import com.example.courseservice.dto.request.review.ReviewCreationRequest;
import com.example.courseservice.dto.response.rerview.ReviewCreationResponse;
import com.example.courseservice.model.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewMapper {
//    @Mapping(target = "review_id", ignore = true)
    @Mapping(target = "rating", source = "rating")
    @Mapping(target = "comment", source = "comment")
    @Mapping(target = "userUid", source = "userUid")
    Review toReview(ReviewCreationRequest request);

    @Mapping(target = "review_id", source = "review_id")
    @Mapping(target = "rating", source = "rating")
    @Mapping(target = "comment", source = "comment")
    @Mapping(target = "userUid", source = "userUid")
    @Mapping(target = "courseId", source = "course.courseId")
    ReviewCreationResponse toReviewCreationResponse(Review review);
}
