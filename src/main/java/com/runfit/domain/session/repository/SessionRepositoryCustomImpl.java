package com.runfit.domain.session.repository;

import static com.runfit.domain.crew.entity.QCrew.crew;
import static com.runfit.domain.session.entity.QSession.session;
import static com.runfit.domain.session.entity.QSessionLike.sessionLike;
import static com.runfit.domain.session.entity.QSessionParticipant.sessionParticipant;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.runfit.domain.session.controller.dto.request.SessionSearchCondition;
import com.runfit.domain.session.controller.dto.response.SessionListResponse;
import com.runfit.domain.session.entity.SessionLevel;
import com.runfit.domain.session.entity.SessionStatus;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class SessionRepositoryCustomImpl implements SessionRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<SessionListResponse> searchSessions(SessionSearchCondition condition, Long userId, Pageable pageable) {
        List<SessionListResponse> content = queryFactory
            .select(Projections.constructor(SessionListResponse.class,
                session.id,
                session.crew.id,
                session.hostUser.userId,
                session.name,
                session.image,
                session.location,
                session.sessionAt,
                session.registerBy,
                session.level,
                session.status,
                session.pace,
                session.maxParticipantCount,
                ExpressionUtils.as(
                    JPAExpressions.select(sessionParticipant.count())
                        .from(sessionParticipant)
                        .where(sessionParticipant.session.eq(session)),
                    "currentParticipantCount"
                ),
                userId != null ?
                    ExpressionUtils.as(
                        JPAExpressions.selectOne()
                            .from(sessionLike)
                            .where(
                                sessionLike.session.eq(session),
                                sessionLike.user.userId.eq(userId)
                            ).exists(),
                        "liked"
                    ) : Expressions.asBoolean(false)
            ))
            .from(session)
            .join(session.crew, crew)
            .where(
                isNotDeleted(),
                cityEq(condition.city()),
                crewIdEq(condition.crewId()),
                levelEq(condition.level()),
                dateEq(condition.date()),
                statusEq(condition.status())
            )
            .orderBy(getOrderSpecifier(condition.sort()))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize() + 1)
            .fetch();

        boolean hasNext = content.size() > pageable.getPageSize();
        if (hasNext) {
            content.remove(content.size() - 1);
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }

    private BooleanExpression isNotDeleted() {
        return session.deleted.isNull();
    }

    private BooleanExpression cityEq(String city) {
        return StringUtils.hasText(city) ? crew.city.eq(city) : null;
    }

    private BooleanExpression crewIdEq(Long crewId) {
        return crewId != null ? session.crew.id.eq(crewId) : null;
    }

    private BooleanExpression levelEq(SessionLevel level) {
        return level != null ? session.level.eq(level) : null;
    }

    private BooleanExpression dateEq(LocalDate date) {
        if (date == null) {
            return null;
        }
        return session.sessionAt.between(
            date.atStartOfDay(),
            date.plusDays(1).atStartOfDay()
        );
    }

    private BooleanExpression statusEq(SessionStatus status) {
        return status != null ? session.status.eq(status) : null;
    }

    private OrderSpecifier<?> getOrderSpecifier(String sort) {
        if (sort == null) {
            return session.sessionAt.asc();
        }

        return switch (sort) {
            case "sessionAtDesc" -> session.sessionAt.desc();
            case "createdAtDesc" -> session.createdAt.desc();
            case "createdAtAsc" -> session.createdAt.asc();
            default -> session.sessionAt.asc();
        };
    }
}
