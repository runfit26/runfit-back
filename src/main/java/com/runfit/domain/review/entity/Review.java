package com.runfit.domain.review.entity;

import com.runfit.common.model.BaseEntity;
import com.runfit.domain.session.entity.Session;
import com.runfit.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "reviews", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"session_id", "user_id"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "ranks", nullable = false)
    private Integer ranks;

    @Column(name = "image")
    private String image;

    @Builder
    private Review(Session session, User user, String description, Integer ranks, String image) {
        this.session = session;
        this.user = user;
        this.description = description;
        this.ranks = ranks;
        this.image = image;
    }

    public static Review create(Session session, User user, String description, Integer ranks, String image) {
        return Review.builder()
            .session(session)
            .user(user)
            .description(description)
            .ranks(ranks)
            .image(image)
            .build();
    }
}
