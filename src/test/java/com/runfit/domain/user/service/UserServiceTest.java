package com.runfit.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.runfit.common.exception.BusinessException;
import com.runfit.common.exception.ErrorCode;
import com.runfit.domain.review.controller.dto.response.ReviewResponse;
import com.runfit.domain.review.service.ReviewService;
import com.runfit.domain.session.entity.SessionLevel;
import com.runfit.domain.session.entity.SessionStatus;
import com.runfit.domain.session.repository.SessionLikeRepository;
import com.runfit.domain.user.controller.dto.request.UserUpdateRequest;
import com.runfit.domain.user.controller.dto.response.LikedSessionResponse;
import com.runfit.domain.user.controller.dto.response.UserProfileResponse;
import com.runfit.domain.user.controller.dto.response.UserResponse;
import com.runfit.domain.user.entity.User;
import com.runfit.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SessionLikeRepository sessionLikeRepository;

    @Mock
    private ReviewService reviewService;

    @Nested
    @DisplayName("내가 찜한 세션 목록 조회")
    class GetMyLikedSessions {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            Long userId = 1L;
            PageRequest pageable = PageRequest.of(0, 10);

            LikedSessionResponse likedSession1 = new LikedSessionResponse(
                1L, 1L, "한강 야간 러닝", "https://example.com/session1.jpg",
                "잠실 한강공원", LocalDateTime.now().plusDays(7),
                SessionLevel.BEGINNER, SessionStatus.OPEN
            );
            LikedSessionResponse likedSession2 = new LikedSessionResponse(
                2L, 2L, "북한산 트레일 러닝", "https://example.com/session2.jpg",
                "북한산 입구", LocalDateTime.now().plusDays(14),
                SessionLevel.ADVANCED, SessionStatus.OPEN
            );

            Slice<LikedSessionResponse> mockSlice = new SliceImpl<>(
                List.of(likedSession1, likedSession2), pageable, false
            );

            given(sessionLikeRepository.findLikedSessionsByUserId(userId, pageable))
                .willReturn(mockSlice);

            // when
            Slice<LikedSessionResponse> result = userService.getMyLikedSessions(userId, pageable);

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.hasNext()).isFalse();
            assertThat(result.getContent().get(0).sessionId()).isEqualTo(1L);
            assertThat(result.getContent().get(0).name()).isEqualTo("한강 야간 러닝");
            assertThat(result.getContent().get(1).sessionId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("성공 - 찜한 세션 없음")
        void success_empty() {
            // given
            Long userId = 1L;
            PageRequest pageable = PageRequest.of(0, 10);

            Slice<LikedSessionResponse> mockSlice = new SliceImpl<>(List.of(), pageable, false);

            given(sessionLikeRepository.findLikedSessionsByUserId(userId, pageable))
                .willReturn(mockSlice);

            // when
            Slice<LikedSessionResponse> result = userService.getMyLikedSessions(userId, pageable);

            // then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.hasNext()).isFalse();
        }

        @Test
        @DisplayName("성공 - 다음 페이지 존재")
        void success_hasNext() {
            // given
            Long userId = 1L;
            PageRequest pageable = PageRequest.of(0, 2);

            LikedSessionResponse likedSession1 = new LikedSessionResponse(
                1L, 1L, "세션1", null, "장소1", LocalDateTime.now().plusDays(7),
                SessionLevel.BEGINNER, SessionStatus.OPEN
            );
            LikedSessionResponse likedSession2 = new LikedSessionResponse(
                2L, 1L, "세션2", null, "장소2", LocalDateTime.now().plusDays(8),
                SessionLevel.INTERMEDIATE, SessionStatus.OPEN
            );

            Slice<LikedSessionResponse> mockSlice = new SliceImpl<>(
                List.of(likedSession1, likedSession2), pageable, true
            );

            given(sessionLikeRepository.findLikedSessionsByUserId(userId, pageable))
                .willReturn(mockSlice);

            // when
            Slice<LikedSessionResponse> result = userService.getMyLikedSessions(userId, pageable);

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.hasNext()).isTrue();
        }
    }

    @Nested
    @DisplayName("내 정보 조회")
    class GetMyInfo {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            Long userId = 1L;
            User user = User.create("user@example.com", "password", "홍길동");

            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // when
            UserResponse result = userService.getMyInfo(userId);

            // then
            assertThat(result.email()).isEqualTo("user@example.com");
            assertThat(result.name()).isEqualTo("홍길동");
        }

        @Test
        @DisplayName("실패 - 사용자 없음")
        void fail_userNotFound() {
            // given
            Long userId = 999L;
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.getMyInfo(userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("내 정보 수정")
    class UpdateMyInfo {

        @Test
        @DisplayName("성공 - 전체 필드 수정")
        void success_allFields() {
            // given
            Long userId = 1L;
            User user = User.create("user@example.com", "password", "홍길동");
            UserUpdateRequest request = new UserUpdateRequest(
                "김철수",
                "https://example.com/new-profile.jpg",
                "새로운 소개",
                "부산",
                420,
                List.of("아침 러닝", "대회 준비")
            );

            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // when
            UserResponse result = userService.updateMyInfo(userId, request);

            // then
            assertThat(result.name()).isEqualTo("김철수");
            assertThat(result.image()).isEqualTo("https://example.com/new-profile.jpg");
            assertThat(result.introduction()).isEqualTo("새로운 소개");
            assertThat(result.city()).isEqualTo("부산");
            assertThat(result.pace()).isEqualTo(420);
            assertThat(result.styles()).containsExactly("아침 러닝", "대회 준비");
        }

        @Test
        @DisplayName("성공 - 부분 필드 수정 (null 필드는 변경 안됨)")
        void success_partialFields() {
            // given
            Long userId = 1L;
            User user = User.create("user@example.com", "password", "홍길동");
            user.update("홍길동", "original-image.jpg", "원래 소개", "서울", 390, List.of("저녁 러닝"));

            UserUpdateRequest request = new UserUpdateRequest(
                "김철수",
                null,
                null,
                null,
                null,
                null
            );

            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // when
            UserResponse result = userService.updateMyInfo(userId, request);

            // then
            assertThat(result.name()).isEqualTo("김철수");
            assertThat(result.image()).isEqualTo("original-image.jpg");
            assertThat(result.introduction()).isEqualTo("원래 소개");
            assertThat(result.city()).isEqualTo("서울");
            assertThat(result.pace()).isEqualTo(390);
            assertThat(result.styles()).containsExactly("저녁 러닝");
        }

        @Test
        @DisplayName("실패 - 사용자 없음")
        void fail_userNotFound() {
            // given
            Long userId = 999L;
            UserUpdateRequest request = new UserUpdateRequest("김철수", null, null, null, null, null);

            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.updateMyInfo(userId, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("특정 유저 정보 조회")
    class GetUserProfile {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            Long userId = 2L;
            User user = User.create("other@example.com", "password", "김러너");
            user.update("김러너", "https://example.com/profile.jpg", "함께 달려요!", "서울", 420, List.of("아침 러닝"));

            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // when
            UserProfileResponse result = userService.getUserProfile(userId);

            // then
            assertThat(result.id()).isEqualTo(user.getUserId());
            assertThat(result.name()).isEqualTo("김러너");
            assertThat(result.image()).isEqualTo("https://example.com/profile.jpg");
            assertThat(result.introduction()).isEqualTo("함께 달려요!");
            assertThat(result.city()).isEqualTo("서울");
            assertThat(result.pace()).isEqualTo(420);
            assertThat(result.styles()).containsExactly("아침 러닝");
        }

        @Test
        @DisplayName("실패 - 사용자 없음")
        void fail_userNotFound() {
            // given
            Long userId = 999L;
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.getUserProfile(userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("내가 작성한 리뷰 목록 조회")
    class GetMyReviews {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            Long userId = 1L;
            PageRequest pageable = PageRequest.of(0, 4);

            ReviewResponse review1 = new ReviewResponse(
                10L, 12L, 3L, userId, "홍길동", "https://example.com/profile.jpg",
                "좋은 분위기에서 즐겁게 뛰었습니다.", 5, "https://example.com/review1.jpg",
                LocalDateTime.now()
            );
            ReviewResponse review2 = new ReviewResponse(
                11L, 13L, 4L, userId, "홍길동", "https://example.com/profile.jpg",
                "다음에 또 참여하고 싶어요.", 4, null,
                LocalDateTime.now()
            );

            Page<ReviewResponse> mockPage = new PageImpl<>(
                List.of(review1, review2), pageable, 2
            );

            given(reviewService.getMyReviews(userId, pageable)).willReturn(mockPage);

            // when
            Page<ReviewResponse> result = userService.getMyReviews(userId, pageable);

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent().get(0).id()).isEqualTo(10L);
            assertThat(result.getContent().get(0).description()).isEqualTo("좋은 분위기에서 즐겁게 뛰었습니다.");
        }

        @Test
        @DisplayName("성공 - 작성한 리뷰 없음")
        void success_empty() {
            // given
            Long userId = 1L;
            PageRequest pageable = PageRequest.of(0, 4);

            Page<ReviewResponse> mockPage = new PageImpl<>(List.of(), pageable, 0);

            given(reviewService.getMyReviews(userId, pageable)).willReturn(mockPage);

            // when
            Page<ReviewResponse> result = userService.getMyReviews(userId, pageable);

            // then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("성공 - 페이지네이션 동작")
        void success_pagination() {
            // given
            Long userId = 1L;
            PageRequest pageable = PageRequest.of(1, 4);

            ReviewResponse review = new ReviewResponse(
                15L, 20L, 5L, userId, "홍길동", null,
                "페이지 2의 리뷰", 3, null, LocalDateTime.now()
            );

            Page<ReviewResponse> mockPage = new PageImpl<>(
                List.of(review), pageable, 5
            );

            given(reviewService.getMyReviews(userId, pageable)).willReturn(mockPage);

            // when
            Page<ReviewResponse> result = userService.getMyReviews(userId, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(5);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.getNumber()).isEqualTo(1);
            assertThat(result.hasPrevious()).isTrue();
        }
    }
}
