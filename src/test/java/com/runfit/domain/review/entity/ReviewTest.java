package com.runfit.domain.review.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.runfit.domain.crew.entity.Crew;
import com.runfit.domain.session.entity.Session;
import com.runfit.domain.session.entity.SessionLevel;
import com.runfit.domain.user.entity.User;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ReviewTest {

    private User user;
    private Crew crew;
    private Session session;

    @BeforeEach
    void setUp() {
        user = User.create("user@test.com", "password", "테스트유저");
        ReflectionTestUtils.setField(user, "userId", 1L);

        crew = Crew.create("테스트 크루", "설명", "서울", null);
        ReflectionTestUtils.setField(crew, "id", 1L);

        User hostUser = User.create("host@test.com", "password", "호스트");
        ReflectionTestUtils.setField(hostUser, "userId", 2L);

        session = Session.create(
            crew, hostUser, "테스트 세션", "세션 설명", null, "장소",
            LocalDateTime.now().plusDays(7),
            LocalDateTime.now().plusDays(6),
            SessionLevel.BEGINNER, 390, 20
        );
        ReflectionTestUtils.setField(session, "id", 1L);
    }

    @Test
    @DisplayName("리뷰 생성 성공")
    void create_success() {
        // given
        String description = "좋은 분위기에서 즐겁게 뛰었습니다.";
        Integer ranks = 5;
        String image = "https://example.com/review.jpg";

        // when
        Review review = Review.create(session, user, description, ranks, image);

        // then
        assertThat(review.getSession()).isEqualTo(session);
        assertThat(review.getUser()).isEqualTo(user);
        assertThat(review.getDescription()).isEqualTo(description);
        assertThat(review.getRanks()).isEqualTo(ranks);
        assertThat(review.getImage()).isEqualTo(image);
    }

    @Test
    @DisplayName("리뷰 생성 성공 - 이미지 없이")
    void create_success_withoutImage() {
        // given
        String description = "좋았습니다.";
        Integer ranks = 4;

        // when
        Review review = Review.create(session, user, description, ranks, null);

        // then
        assertThat(review.getSession()).isEqualTo(session);
        assertThat(review.getUser()).isEqualTo(user);
        assertThat(review.getDescription()).isEqualTo(description);
        assertThat(review.getRanks()).isEqualTo(ranks);
        assertThat(review.getImage()).isNull();
    }

    @Test
    @DisplayName("리뷰 생성 시 평점 범위 확인")
    void create_ranks_boundary() {
        // given & when
        Review minRanksReview = Review.create(session, user, "최소 평점", 1, null);
        Review maxRanksReview = Review.create(session, user, "최대 평점", 5, null);

        // then
        assertThat(minRanksReview.getRanks()).isEqualTo(1);
        assertThat(maxRanksReview.getRanks()).isEqualTo(5);
    }
}
