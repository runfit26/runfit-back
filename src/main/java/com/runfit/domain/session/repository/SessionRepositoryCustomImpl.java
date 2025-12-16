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
import com.runfit.domain.session.controller.dto.response.CoordsResponse;
import com.runfit.domain.session.controller.dto.response.SessionListResponse;
import com.runfit.domain.session.entity.SessionLevel;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

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
                    ) : Expressions.asBoolean(false),
                session.createdAt
            ))
            .from(session)
            .join(session.crew, crew)
            .where(
                isNotDeleted(),
                citiesIn(condition.cities()),
                districtsIn(condition.districts()),
                crewIdEq(condition.crewId()),
                levelEq(condition.level()),
                sessionAtDateBetween(condition.dateFrom(), condition.dateTo()),
                sessionAtTimeBetween(condition.timeFrom(), condition.timeTo())
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

    private BooleanExpression citiesIn(List<String> cities) {
        return CollectionUtils.isEmpty(cities) ? null : session.city.in(cities);
    }

    private BooleanExpression districtsIn(List<String> districts) {
        return CollectionUtils.isEmpty(districts) ? null : session.district.in(districts);
    }

    private BooleanExpression crewIdEq(Long crewId) {
        return crewId != null ? session.crew.id.eq(crewId) : null;
    }

    private BooleanExpression levelEq(SessionLevel level) {
        return level != null ? session.level.eq(level) : null;
    }

    private BooleanExpression sessionAtDateBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return null;
        }
        if (startDate != null && endDate != null) {
            return session.sessionAt.between(
                startDate.atStartOfDay(),
                endDate.plusDays(1).atStartOfDay()
            );
        }
        if (startDate != null) {
            return session.sessionAt.goe(startDate.atStartOfDay());
        }
        return session.sessionAt.lt(endDate.plusDays(1).atStartOfDay());
    }

    private BooleanExpression sessionAtTimeBetween(LocalTime startTime, LocalTime endTime) {
        if (startTime == null && endTime == null) {
            return null;
        }

        var timeExpression = Expressions.timeTemplate(LocalTime.class, "CAST({0} AS time)", session.sessionAt);

        if (startTime != null && endTime != null) {
            return timeExpression.between(startTime, endTime);
        }
        if (startTime != null) {
            return timeExpression.goe(startTime);
        }
        return timeExpression.loe(endTime);
    }

    private OrderSpecifier<?> getOrderSpecifier(String sort) {
        if (sort == null) {
            return session.createdAt.desc();
        }

        return switch (sort) {
            case "sessionAtAsc" -> session.sessionAt.asc();
            case "registerByAsc" -> session.registerBy.asc();
            default -> session.createdAt.desc();
        };
    }

    @Override
    public Slice<SessionListResponse> findMyHostedSessions(Long hostUserId, Pageable pageable) {
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
                    JPAExpressions.select(sessionParticipant.count())
                        .from(sessionParticipant)
                        .where(sessionParticipant.session.eq(session)),
                    "currentParticipantCount"
                ),
                ExpressionUtils.as(
                    JPAExpressions.selectOne()
                        .from(sessionLike)
                        .where(
                            sessionLike.session.eq(session),
                            sessionLike.user.userId.eq(hostUserId)
                        ).exists(),
                    "liked"
                ),
                session.createdAt
            ))
            .from(session)
            .join(session.crew, crew)
            .where(
                isNotDeleted(),
                session.hostUser.userId.eq(hostUserId)
            )
            .orderBy(session.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize() + 1)
            .fetch();

        boolean hasNext = content.size() > pageable.getPageSize();
        if (hasNext) {
            content.remove(content.size() - 1);
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }
}
