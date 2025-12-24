package com.runfit.domain.session.repository;

import static com.runfit.domain.crew.entity.QCrew.crew;
import static com.runfit.domain.review.entity.QReview.review;
import static com.runfit.domain.session.entity.QSession.session;
import static com.runfit.domain.session.entity.QSessionLike.sessionLike;
import static com.runfit.domain.session.entity.QSessionParticipant.sessionParticipant;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.runfit.domain.session.controller.dto.response.CoordsResponse;
import com.runfit.domain.session.controller.dto.response.SessionListResponse;
import com.runfit.domain.session.controller.dto.response.SessionParticipantResponse;
import com.runfit.domain.session.entity.QSessionParticipant;
import com.runfit.domain.session.entity.SessionParticipant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SessionParticipantRepositoryCustomImpl implements SessionParticipantRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<SessionListResponse> findParticipatingSessionsByUserId(
        Long userId, String status, Pageable pageable) {

        QSessionParticipant participantCount = new QSessionParticipant("participantCount");
        QSessionParticipant participantCheck = new QSessionParticipant("participantCheck");

        List<SessionListResponse> content = queryFactory
            .select(Projections.constructor(SessionListResponse.class,
                session.id,
                session.crew.id,
                session.hostUser.userId,
                session.name,
                session.image,
                session.city,
                session.district,
                session.location,
                Projections.constructor(CoordsResponse.class,
                    session.latitude,
                    session.longitude
                ),
                session.sessionAt,
                session.registerBy,
                session.level,
                session.status,
                session.pace,
                session.maxParticipantCount,
                ExpressionUtils.as(
                    JPAExpressions.select(participantCount.count())
                        .from(participantCount)
                        .where(participantCount.session.eq(session)),
                    "currentParticipantCount"
                ),
                ExpressionUtils.as(
                    JPAExpressions.selectOne()
                        .from(sessionLike)
                        .where(
                            sessionLike.session.eq(session),
                            sessionLike.user.userId.eq(userId)
                        ).exists(),
                    "liked"
                ),
                session.createdAt,
                ExpressionUtils.as(
                    Expressions.numberTemplate(Double.class,
                        "ROUND({0}, 1)",
                        JPAExpressions.select(review.ranks.avg())
                            .from(review)
                            .where(review.session.eq(session))
                    ),
                    "ranks"
                ),
                Expressions.constant(Collections.<SessionParticipantResponse>emptyList())
            ))
            .from(session)
            .join(session.crew, crew)
            .where(
                session.deleted.isNull(),
                statusFilter(status),
                isHostOrParticipant(userId, participantCheck)
            )
            .orderBy(session.sessionAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize() + 1)
            .fetch();

        boolean hasNext = content.size() > pageable.getPageSize();
        if (hasNext) {
            content.remove(content.size() - 1);
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }

    private BooleanExpression isHostOrParticipant(Long userId, QSessionParticipant participantCheck) {
        return session.hostUser.userId.eq(userId)
            .or(JPAExpressions.selectOne()
                .from(participantCheck)
                .where(
                    participantCheck.session.eq(session),
                    participantCheck.user.userId.eq(userId)
                ).exists());
    }

    private BooleanExpression statusFilter(String status) {
        if (status == null) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        return switch (status.toUpperCase()) {
            case "SCHEDULED" -> session.sessionAt.after(now);
            case "COMPLETED" -> session.sessionAt.before(now);
            default -> null;
        };
    }

    @Override
    public List<SessionParticipant> findParticipantsBySessionIds(List<Long> sessionIds) {
        if (sessionIds == null || sessionIds.isEmpty()) {
            return Collections.emptyList();
        }

        return queryFactory
            .selectFrom(sessionParticipant)
            .join(sessionParticipant.user).fetchJoin()
            .where(sessionParticipant.session.id.in(sessionIds))
            .orderBy(sessionParticipant.joinedAt.desc())
            .fetch();
    }
}
