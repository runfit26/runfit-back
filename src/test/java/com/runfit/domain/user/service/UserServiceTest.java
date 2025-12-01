package com.runfit.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.runfit.domain.session.entity.SessionLevel;
import com.runfit.domain.session.entity.SessionStatus;
import com.runfit.domain.session.repository.SessionLikeRepository;
import com.runfit.domain.user.controller.dto.response.LikedSessionResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private SessionLikeRepository sessionLikeRepository;

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
}
