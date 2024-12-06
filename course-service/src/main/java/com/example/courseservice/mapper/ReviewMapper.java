package com.example.courseservice.mapper;


import com.example.courseservice.dto.request.course.CourseCreationRequest;
import com.example.courseservice.dto.request.review.ReviewCreationRequest;
import com.example.courseservice.dto.response.rerview.ReviewCreationResponse;
import com.example.courseservice.model.Course;
import com.example.courseservice.model.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rating", source = "rating")
    @Mapping(target = "comment", source = "comment")
    @Mapping(target = "userUid", source = "userUid")
    Review toReview(ReviewCreationRequest request);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "rating", source = "rating")
    @Mapping(target = "comment", source = "comment")
    @Mapping(target = "userUid", source = "userUid")
    @Mapping(target = "courseId", source = "course.id")
    ReviewCreationResponse toReviewCreationResponse(Review review);
}
