package com.runfit.domain.crew.repository;

import static com.runfit.domain.crew.entity.QCrew.crew;
import static com.runfit.domain.crew.entity.QMembership.membership;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.runfit.domain.crew.controller.dto.response.CrewListResponse;
import com.runfit.domain.crew.entity.CrewRole;
import com.runfit.domain.crew.entity.QMembership;
import com.runfit.domain.user.controller.dto.response.MyCrewResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MembershipRepositoryCustomImpl implements MembershipRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<CrewListResponse> findOwnedCrewsByUserId(Long userId, Pageable pageable) {
        QMembership membershipCount = new QMembership("membershipCount");

        List<CrewListResponse> content = queryFactory
            .select(Projections.constructor(CrewListResponse.class,
                crew.id,
                crew.name,
                crew.description,
                crew.city,
                crew.image,
                JPAExpressions.select(membershipCount.count())
                    .from(membershipCount)
                    .where(membershipCount.crew.eq(crew)),
                crew.createdAt
            ))
            .from(membership)
            .join(membership.crew, crew)
            .where(
                membership.user.userId.eq(userId),
                membership.role.eq(CrewRole.LEADER),
                crew.deleted.isNull()
            )
            .orderBy(crew.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize() + 1)
            .fetch();

        boolean hasNext = content.size() > pageable.getPageSize();
        if (hasNext) {
            content.remove(content.size() - 1);
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }

    @Override
    public Slice<MyCrewResponse> findMyCrewsByUserId(Long userId, Pageable pageable) {
        QMembership membershipCount = new QMembership("membershipCount");

        List<MyCrewResponse> content = queryFactory
            .select(Projections.constructor(MyCrewResponse.class,
                crew.id,
                crew.name,
                crew.description,
                crew.city,
                crew.image,
                JPAExpressions.select(membershipCount.count())
                    .from(membershipCount)
                    .where(membershipCount.crew.eq(crew)),
                membership.role,
                crew.createdAt
            ))
            .from(membership)
            .join(membership.crew, crew)
            .where(
                membership.user.userId.eq(userId),
                crew.deleted.isNull()
            )
            .orderBy(membership.joinedAt.desc())
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
