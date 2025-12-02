package com.runfit.domain.review.repository;

import static com.runfit.domain.review.entity.QReview.review;
import static com.runfit.domain.session.entity.QSession.session;
import static com.runfit.domain.user.entity.QUser.user;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.runfit.domain.review.controller.dto.response.CrewReviewResponse;
import com.runfit.domain.review.controller.dto.response.ReviewResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryCustomImpl implements ReviewRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ReviewResponse> findReviewsBySessionId(Long sessionId, Pageable pageable) {
        List<ReviewResponse> content = queryFactory
            .select(Projections.constructor(ReviewResponse.class,
                review.id,
                review.session.id,
                session.crew.id,
                review.user.userId,
                user.name,
                user.image,
                review.description,
                review.ranks,
                review.image,
                review.createdAt
            ))
            .from(review)
            .join(review.session, session)
            .join(review.user, user)
            .where(review.session.id.eq(sessionId))
            .orderBy(review.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long total = queryFactory
            .select(review.count())
            .from(review)
            .where(review.session.id.eq(sessionId))
            .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    @Override
    public Page<ReviewResponse> findReviewsByUserId(Long userId, Pageable pageable) {
        List<ReviewResponse> content = queryFactory
            .select(Projections.constructor(ReviewResponse.class,
                review.id,
                review.session.id,
                session.crew.id,
                review.user.userId,
                user.name,
                user.image,
                review.description,
                review.ranks,
                review.image,
                review.createdAt
            ))
            .from(review)
            .join(review.session, session)
            .join(review.user, user)
            .where(review.user.userId.eq(userId))
            .orderBy(review.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long total = queryFactory
            .select(review.count())
            .from(review)
            .where(review.user.userId.eq(userId))
            .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    @Override
    public Page<CrewReviewResponse> findReviewsByCrewId(Long crewId, Pageable pageable) {
        List<CrewReviewResponse> content = queryFactory
            .select(Projections.constructor(CrewReviewResponse.class,
                review.id,
                review.session.id,
                session.name,
                session.crew.id,
                review.user.userId,
                user.name,
                user.image,
                review.description,
                review.ranks,
                review.image,
                review.createdAt
            ))
            .from(review)
            .join(review.session, session)
            .join(review.user, user)
            .where(session.crew.id.eq(crewId))
            .orderBy(review.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long total = queryFactory
            .select(review.count())
            .from(review)
            .join(review.session, session)
            .where(session.crew.id.eq(crewId))
            .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }
}
