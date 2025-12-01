package com.runfit.domain.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.runfit.common.exception.BusinessException;
import com.runfit.common.exception.ErrorCode;
import com.runfit.domain.crew.entity.Crew;
import com.runfit.domain.review.controller.dto.request.ReviewCreateRequest;
import com.runfit.domain.review.controller.dto.response.ReviewDeleteResponse;
import com.runfit.domain.review.controller.dto.response.ReviewResponse;
import com.runfit.domain.review.entity.Review;
import com.runfit.domain.review.repository.ReviewRepository;
import com.runfit.domain.session.entity.Session;
import com.runfit.domain.session.entity.SessionLevel;
import com.runfit.domain.session.repository.SessionParticipantRepository;
import com.runfit.domain.session.repository.SessionRepository;
import com.runfit.domain.user.entity.User;
import com.runfit.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @InjectMocks
    private ReviewService reviewService;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private SessionParticipantRepository sessionParticipantRepository;

    @Mock
    private UserRepository userRepository;

    private User user;
    private User otherUser;
    private Crew crew;
    private Session session;
    private Review review;

    @BeforeEach
    void setUp() {
        user = User.create("user@test.com", "password", "테스트유저");
        ReflectionTestUtils.setField(user, "userId", 1L);

        otherUser = User.create("other@test.com", "password", "다른유저");
        ReflectionTestUtils.setField(otherUser, "userId", 2L);

        crew = Crew.create("테스트 크루", "설명", "서울", null);
        ReflectionTestUtils.setField(crew, "id", 1L);

        User hostUser = User.create("host@test.com", "password", "호스트");
        ReflectionTestUtils.setField(hostUser, "userId", 3L);

        session = Session.create(
            crew, hostUser, "테스트 세션", "세션 설명", null, "장소",
            LocalDateTime.now().plusDays(7),
            LocalDateTime.now().plusDays(6),
            SessionLevel.BEGINNER, 390, 20
        );
        ReflectionTestUtils.setField(session, "id", 1L);

        review = Review.create(session, user, "좋았습니다!", 5, null);
        ReflectionTestUtils.setField(review, "id", 1L);
        ReflectionTestUtils.setField(review, "createdAt", LocalDateTime.now());
    }

    @Nested
    @DisplayName("리뷰 작성")
    class CreateReview {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            ReviewCreateRequest request = new ReviewCreateRequest(
                "좋은 분위기에서 즐겁게 뛰었습니다.",
                5,
                "https://example.com/review.jpg"
            );
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(sessionRepository.findByIdAndNotDeleted(1L)).willReturn(Optional.of(session));
            given(sessionParticipantRepository.existsBySessionAndUser(session, user)).willReturn(true);
            given(reviewRepository.existsBySessionAndUser(session, user)).willReturn(false);
            given(reviewRepository.save(any(Review.class))).willAnswer(invocation -> {
                Review saved = invocation.getArgument(0);
                ReflectionTestUtils.setField(saved, "id", 1L);
                ReflectionTestUtils.setField(saved, "createdAt", LocalDateTime.now());
                return saved;
            });

            // when
            ReviewResponse response = reviewService.createReview(1L, 1L, request);

            // then
            assertThat(response.description()).isEqualTo("좋은 분위기에서 즐겁게 뛰었습니다.");
            assertThat(response.ranks()).isEqualTo(5);
            assertThat(response.sessionId()).isEqualTo(1L);
            assertThat(response.userId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("실패 - 세션 미참가자")
        void fail_notParticipant() {
            // given
            ReviewCreateRequest request = new ReviewCreateRequest("리뷰", 5, null);
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(sessionRepository.findByIdAndNotDeleted(1L)).willReturn(Optional.of(session));
            given(sessionParticipantRepository.existsBySessionAndUser(session, user)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> reviewService.createReview(1L, 1L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_SESSION_PARTICIPANT);
        }

        @Test
        @DisplayName("실패 - 이미 리뷰 작성함")
        void fail_alreadyReviewed() {
            // given
            ReviewCreateRequest request = new ReviewCreateRequest("리뷰", 5, null);
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(sessionRepository.findByIdAndNotDeleted(1L)).willReturn(Optional.of(session));
            given(sessionParticipantRepository.existsBySessionAndUser(session, user)).willReturn(true);
            given(reviewRepository.existsBySessionAndUser(session, user)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> reviewService.createReview(1L, 1L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_REVIEWED_SESSION);
        }

        @Test
        @DisplayName("실패 - 세션 없음")
        void fail_sessionNotFound() {
            // given
            ReviewCreateRequest request = new ReviewCreateRequest("리뷰", 5, null);
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(sessionRepository.findByIdAndNotDeleted(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reviewService.createReview(1L, 999L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SESSION_NOT_FOUND);
        }

        @Test
        @DisplayName("실패 - 사용자 없음")
        void fail_userNotFound() {
            // given
            ReviewCreateRequest request = new ReviewCreateRequest("리뷰", 5, null);
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reviewService.createReview(999L, 1L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("리뷰 삭제")
    class DeleteReview {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            given(reviewRepository.findByIdWithSessionAndUser(1L)).willReturn(Optional.of(review));

            // when
            ReviewDeleteResponse response = reviewService.deleteReview(1L, 1L);

            // then
            assertThat(response.message()).contains("삭제");
            verify(reviewRepository).delete(review);
        }

        @Test
        @DisplayName("실패 - 리뷰 없음")
        void fail_reviewNotFound() {
            // given
            given(reviewRepository.findByIdWithSessionAndUser(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reviewService.deleteReview(1L, 999L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REVIEW_NOT_FOUND);
        }

        @Test
        @DisplayName("실패 - 삭제 권한 없음")
        void fail_forbidden() {
            // given
            given(reviewRepository.findByIdWithSessionAndUser(1L)).willReturn(Optional.of(review));

            // when & then
            assertThatThrownBy(() -> reviewService.deleteReview(2L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REVIEW_FORBIDDEN);
        }
    }
}
