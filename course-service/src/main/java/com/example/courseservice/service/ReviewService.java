package com.example.courseservice.service;

import com.example.courseservice.dto.request.review.ReviewCreationRequest;
import com.example.courseservice.dto.response.rerview.ReviewCreationResponse;
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

    public ReviewCreationResponse createReview(ReviewCreationRequest request) {

        return null;
    }
}
