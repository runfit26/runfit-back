package com.runfit.domain.session.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.runfit.domain.crew.entity.Crew;
import com.runfit.domain.user.entity.User;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class SessionTest {

    private User hostUser;
    private Crew crew;

    @BeforeEach
    void setUp() {
        hostUser = User.create("host@test.com", "password", "호스트");
        ReflectionTestUtils.setField(hostUser, "userId", 1L);

        crew = Crew.create("테스트 크루", "설명", "서울", null);
        ReflectionTestUtils.setField(crew, "id", 1L);
    }

    @Test
    @DisplayName("세션 생성 성공")
    void create_success() {
        // given
        String name = "한강 야간 러닝";
        String description = "5km 가볍게 뛰어요.";
        String image = "https://example.com/session.jpg";
        String city = "서울";
        String district = "송파구";
        String location = "반포 한강공원 (서울 서초구 신반포로11길 40)";
        Double latitude = 37.5145;
        Double longitude = 127.1017;
        LocalDateTime sessionAt = LocalDateTime.now().plusDays(7);
        LocalDateTime registerBy = LocalDateTime.now().plusDays(6);
        SessionLevel level = SessionLevel.BEGINNER;
        Integer pace = 390;
        Integer maxParticipantCount = 20;

        // when
        Session session = Session.create(
            crew, hostUser, name, description, image,
            city, district, location, latitude, longitude,
            sessionAt, registerBy, level, pace, maxParticipantCount
        );

        // then
        assertThat(session.getCrew()).isEqualTo(crew);
        assertThat(session.getHostUser()).isEqualTo(hostUser);
        assertThat(session.getName()).isEqualTo(name);
        assertThat(session.getDescription()).isEqualTo(description);
        assertThat(session.getImage()).isEqualTo(image);
        assertThat(session.getCity()).isEqualTo(city);
        assertThat(session.getDistrict()).isEqualTo(district);
        assertThat(session.getLocation()).isEqualTo(location);
        assertThat(session.getLatitude()).isEqualTo(latitude);
        assertThat(session.getLongitude()).isEqualTo(longitude);
        assertThat(session.getSessionAt()).isEqualTo(sessionAt);
        assertThat(session.getRegisterBy()).isEqualTo(registerBy);
        assertThat(session.getLevel()).isEqualTo(level);
        assertThat(session.getStatus()).isEqualTo(SessionStatus.OPEN);
        assertThat(session.getPace()).isEqualTo(pace);
        assertThat(session.getMaxParticipantCount()).isEqualTo(maxParticipantCount);
    }

    @Test
    @DisplayName("세션 마감 성공")
    void close_success() {
        // given
        Session session = createTestSession();
        assertThat(session.isOpen()).isTrue();

        // when
        session.close();

        // then
        assertThat(session.isOpen()).isFalse();
        assertThat(session.getStatus()).isEqualTo(SessionStatus.CLOSED);
    }

    @Test
    @DisplayName("세션 오픈 성공")
    void open_success() {
        // given
        Session session = createTestSession();
        session.close();
        assertThat(session.isOpen()).isFalse();

        // when
        session.open();

        // then
        assertThat(session.isOpen()).isTrue();
        assertThat(session.getStatus()).isEqualTo(SessionStatus.OPEN);
    }

    @Test
    @DisplayName("신청 가능 여부 확인 - 오픈 상태 & 마감 전")
    void isRegistrationOpen_true() {
        // given
        Session session = Session.create(
            crew, hostUser, "세션", "설명", null,
            "서울", "송파구", null, 37.5145, 127.1017,
            LocalDateTime.now().plusDays(7),
            LocalDateTime.now().plusDays(6),
            SessionLevel.BEGINNER, 390, 20
        );

        // when & then
        assertThat(session.isRegistrationOpen()).isTrue();
    }

    @Test
    @DisplayName("신청 불가 - 마감 시간 지남")
    void isRegistrationOpen_false_afterDeadline() {
        // given
        Session session = Session.create(
            crew, hostUser, "세션", "설명", null,
            "서울", "송파구", null, 37.5145, 127.1017,
            LocalDateTime.now().plusDays(7),
            LocalDateTime.now().minusDays(1),
            SessionLevel.BEGINNER, 390, 20
        );

        // when & then
        assertThat(session.isRegistrationOpen()).isFalse();
    }

    @Test
    @DisplayName("신청 불가 - 세션이 마감됨")
    void isRegistrationOpen_false_closed() {
        // given
        Session session = Session.create(
            crew, hostUser, "세션", "설명", null,
            "서울", "송파구", null, 37.5145, 127.1017,
            LocalDateTime.now().plusDays(7),
            LocalDateTime.now().plusDays(6),
            SessionLevel.BEGINNER, 390, 20
        );
        session.close();

        // when & then
        assertThat(session.isRegistrationOpen()).isFalse();
    }

    private Session createTestSession() {
        return Session.create(
            crew, hostUser, "테스트 세션", "설명", null,
            "서울", "송파구", null, 37.5145, 127.1017,
            LocalDateTime.now().plusDays(7),
            LocalDateTime.now().plusDays(6),
            SessionLevel.BEGINNER, 390, 20
        );
    }
}
