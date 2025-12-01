package com.runfit.domain.session.repository;

import static com.runfit.domain.session.entity.QSession.session;
import static com.runfit.domain.session.entity.QSessionLike.sessionLike;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.runfit.domain.user.controller.dto.response.LikedSessionResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SessionLikeRepositoryCustomImpl implements SessionLikeRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<LikedSessionResponse> findLikedSessionsByUserId(Long userId, Pageable pageable) {
        List<LikedSessionResponse> content = queryFactory
            .select(Projections.constructor(LikedSessionResponse.class,
                session.id,
                session.crew.id,
                session.name,
                session.image,
                session.location,
                session.sessionAt,
                session.level,
                session.status
            ))
            .from(sessionLike)
            .join(sessionLike.session, session)
            .where(
                sessionLike.user.userId.eq(userId),
                session.deleted.isNull()
            )
            .orderBy(sessionLike.likedAt.desc())
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
