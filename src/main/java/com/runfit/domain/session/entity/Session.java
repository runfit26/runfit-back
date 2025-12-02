package com.runfit.domain.session.entity;

import com.runfit.common.model.SoftDeleteEntity;
import com.runfit.domain.crew.entity.Crew;
import com.runfit.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "sessions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Session extends SoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crew_id", nullable = false)
    private Crew crew;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_user_id", nullable = false)
    private User hostUser;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "image")
    private String image;

    @Column(name = "location")
    private String location;

    @Column(name = "session_at", nullable = false)
    private LocalDateTime sessionAt;

    @Column(name = "register_by", nullable = false)
    private LocalDateTime registerBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false)
    private SessionLevel level;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SessionStatus status;

    @Column(name = "pace")
    private Integer pace;

    @Column(name = "max_participant_count", nullable = false)
    private Integer maxParticipantCount;

    @Builder
    private Session(Crew crew, User hostUser, String name, String description, String image,
        String location, LocalDateTime sessionAt, LocalDateTime registerBy,
        SessionLevel level, Integer pace, Integer maxParticipantCount) {
        this.crew = crew;
        this.hostUser = hostUser;
        this.name = name;
        this.description = description;
        this.image = image;
        this.location = location;
        this.sessionAt = sessionAt;
        this.registerBy = registerBy;
        this.level = level;
        this.status = SessionStatus.OPEN;
        this.pace = pace;
        this.maxParticipantCount = maxParticipantCount;
    }

    public static Session create(Crew crew, User hostUser, String name, String description,
        String image, String location, LocalDateTime sessionAt, LocalDateTime registerBy,
        SessionLevel level, Integer pace, Integer maxParticipantCount) {
        return Session.builder()
            .crew(crew)
            .hostUser(hostUser)
            .name(name)
            .description(description)
            .image(image)
            .location(location)
            .sessionAt(sessionAt)
            .registerBy(registerBy)
            .level(level)
            .pace(pace)
            .maxParticipantCount(maxParticipantCount)
            .build();
    }

    public void close() {
        this.status = SessionStatus.CLOSED;
    }

    public void open() {
        this.status = SessionStatus.OPEN;
    }

    public boolean isOpen() {
        return this.status == SessionStatus.OPEN;
    }

    public boolean isRegistrationOpen() {
        return isOpen() && LocalDateTime.now().isBefore(registerBy);
    }

    public void update(String name, String description, String image, String location,
        LocalDateTime sessionAt, LocalDateTime registerBy, SessionLevel level,
        Integer pace, Integer maxParticipantCount) {
        this.name = name;
        this.description = description;
        this.image = image;
        this.location = location;
        this.sessionAt = sessionAt;
        this.registerBy = registerBy;
        this.level = level;
        this.pace = pace;
        this.maxParticipantCount = maxParticipantCount;
    }
}
