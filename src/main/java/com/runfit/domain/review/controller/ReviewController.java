package com.runfit.domain.review.controller;

import com.runfit.common.response.PageResponse;
import com.runfit.common.response.ResponseWrapper;
import com.runfit.domain.auth.model.AuthUser;
import com.runfit.domain.review.controller.dto.request.ReviewCreateRequest;
import com.runfit.domain.review.controller.dto.response.ReviewDeleteResponse;
import com.runfit.domain.review.controller.dto.response.ReviewResponse;
import com.runfit.domain.review.service.ReviewService;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReviewController implements ReviewApi {

    private final ReviewService reviewService;

    @Override
    @GetMapping("/api/sessions/{sessionId}/reviews")
    public ResponseEntity<ResponseWrapper<PageResponse<ReviewResponse>>> getSessionReviews(
        @PathVariable Long sessionId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Page<ReviewResponse> result = reviewService.getSessionReviews(sessionId, PageRequest.of(page, size));
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.from(result)));
    }

    @Override
    @PostMapping("/api/sessions/{sessionId}/reviews")
    public ResponseEntity<ResponseWrapper<ReviewResponse>> createReview(
        @AuthenticationPrincipal AuthUser user,
        @PathVariable Long sessionId,
        @Valid @RequestBody ReviewCreateRequest request
    ) {
        ReviewResponse response = reviewService.createReview(user.userId(), sessionId, request);
        URI location = URI.create("/api/reviews/" + response.id());
        return ResponseEntity.created(location).body(ResponseWrapper.success(response));
    }

    @Override
    @DeleteMapping("/api/reviews/{reviewId}")
    public ResponseEntity<ResponseWrapper<ReviewDeleteResponse>> deleteReview(
        @AuthenticationPrincipal AuthUser user,
        @PathVariable Long reviewId
    ) {
        ReviewDeleteResponse response = reviewService.deleteReview(user.userId(), reviewId);
        return ResponseEntity.ok(ResponseWrapper.success(response));
    }
}
