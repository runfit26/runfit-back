package com.runfit.domain.review.service;

import com.runfit.common.exception.BusinessException;
import com.runfit.common.exception.ErrorCode;
import com.runfit.domain.crew.repository.CrewRepository;
import com.runfit.domain.review.controller.dto.request.ReviewCreateRequest;
import com.runfit.domain.review.controller.dto.response.CrewReviewResponse;
import com.runfit.domain.review.controller.dto.response.ReviewDeleteResponse;
import com.runfit.domain.review.controller.dto.response.ReviewResponse;
import com.runfit.domain.review.entity.Review;
import com.runfit.domain.review.repository.ReviewRepository;
import com.runfit.domain.session.entity.Session;
import com.runfit.domain.session.repository.SessionParticipantRepository;
import com.runfit.domain.session.repository.SessionRepository;
import com.runfit.domain.user.entity.User;
import com.runfit.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final SessionRepository sessionRepository;
    private final SessionParticipantRepository sessionParticipantRepository;
    private final UserRepository userRepository;
    private final CrewRepository crewRepository;

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getSessionReviews(Long sessionId, Pageable pageable) {
        findSessionById(sessionId);
        return reviewRepository.findReviewsBySessionId(sessionId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getMyReviews(Long userId, Pageable pageable) {
        return reviewRepository.findReviewsByUserId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<CrewReviewResponse> getCrewReviews(Long crewId, Pageable pageable) {
        crewRepository.findByIdAndDeletedIsNull(crewId)
            .orElseThrow(() -> new BusinessException(ErrorCode.CREW_NOT_FOUND));
        return reviewRepository.findReviewsByCrewId(crewId, pageable);
    }

    @Transactional
    public ReviewResponse createReview(Long userId, Long sessionId, ReviewCreateRequest request) {
        User user = findUserById(userId);
        Session session = findSessionById(sessionId);

        if (!sessionParticipantRepository.existsBySessionAndUser(session, user)) {
            throw new BusinessException(ErrorCode.NOT_SESSION_PARTICIPANT);
        }

        if (reviewRepository.existsBySessionAndUser(session, user)) {
            throw new BusinessException(ErrorCode.ALREADY_REVIEWED_SESSION);
        }

        Review review = Review.create(
            session,
            user,
            request.description(),
            request.ranks(),
            request.image()
        );

        Review savedReview = reviewRepository.save(review);

        return ReviewResponse.from(savedReview);
    }

    @Transactional
    public ReviewDeleteResponse deleteReview(Long userId, Long reviewId, boolean isAdmin) {
        Review review = reviewRepository.findByIdWithSessionAndUser(reviewId)
            .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

        if (!isAdmin && !review.getUser().getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.REVIEW_FORBIDDEN);
        }

        reviewRepository.delete(review);

        return ReviewDeleteResponse.deleted();
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private Session findSessionById(Long sessionId) {
        return sessionRepository.findByIdAndNotDeleted(sessionId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));
    }
}
