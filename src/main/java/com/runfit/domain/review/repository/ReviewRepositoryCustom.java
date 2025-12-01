package com.runfit.domain.review.repository;

import com.runfit.domain.review.controller.dto.response.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewRepositoryCustom {

    Page<ReviewResponse> findReviewsBySessionId(Long sessionId, Pageable pageable);

    Page<ReviewResponse> findReviewsByUserId(Long userId, Pageable pageable);
}
