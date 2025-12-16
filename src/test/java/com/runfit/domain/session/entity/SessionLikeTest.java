package com.runfit.domain.session.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.runfit.domain.crew.entity.Crew;
import com.runfit.domain.user.entity.User;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class SessionLikeTest {

    private User user;
    private Session session;

    @BeforeEach
    void setUp() {
        User hostUser = User.create("host@test.com", "password", "호스트");
        ReflectionTestUtils.setField(hostUser, "userId", 1L);

        Crew crew = Crew.create("테스트 크루", "설명", "서울", null);
        ReflectionTestUtils.setField(crew, "id", 1L);

        user = User.create("liker@test.com", "password", "찜한사용자");
        ReflectionTestUtils.setField(user, "userId", 2L);

        session = Session.create(
            crew, hostUser, "테스트 세션", "설명", null,
            "서울", "송파구", null, 37.5145, 127.1017,
            LocalDateTime.now().plusDays(7),
            LocalDateTime.now().plusDays(6),
            SessionLevel.BEGINNER, 390, 20
        );
        ReflectionTestUtils.setField(session, "id", 1L);
    }

    @Test
    @DisplayName("세션 찜 생성 성공")
    void create_success() {
        // when
        SessionLike sessionLike = SessionLike.create(session, user);

        // then
        assertThat(sessionLike.getSession()).isEqualTo(session);
        assertThat(sessionLike.getUser()).isEqualTo(user);
    }
}
