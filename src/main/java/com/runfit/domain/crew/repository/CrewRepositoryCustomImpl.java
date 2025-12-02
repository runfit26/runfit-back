package com.runfit.domain.crew.repository;

import static com.runfit.domain.crew.entity.QCrew.crew;
import static com.runfit.domain.crew.entity.QMembership.membership;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.runfit.domain.crew.controller.dto.request.CrewSearchCondition;
import com.runfit.domain.crew.controller.dto.response.CrewListResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class CrewRepositoryCustomImpl implements CrewRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<CrewListResponse> searchCrews(CrewSearchCondition condition, Pageable pageable) {
        List<CrewListResponse> content = queryFactory
            .select(Projections.constructor(CrewListResponse.class,
                crew.id,
                crew.name,
                crew.description,
                crew.city,
                crew.image,
                membership.count(),
                crew.createdAt
            ))
            .from(crew)
            .leftJoin(membership).on(membership.crew.eq(crew))
            .where(
                isNotDeleted(),
                cityEq(condition.city()),
                keywordContains(condition.keyword())
            )
            .groupBy(crew.id)
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
        return crew.deleted.isNull();
    }

    private BooleanExpression cityEq(String city) {
        return StringUtils.hasText(city) ? crew.city.eq(city) : null;
    }

    private BooleanExpression keywordContains(String keyword) {
        return StringUtils.hasText(keyword) ? crew.name.containsIgnoreCase(keyword) : null;
    }

    private OrderSpecifier<?> getOrderSpecifier(String sort) {
        if (sort == null) {
            return crew.createdAt.desc();
        }

        return switch (sort) {
            case "memberCountDesc" -> membership.count().desc();
            case "createdAtAsc" -> crew.createdAt.asc();
            default -> crew.createdAt.desc();
        };
    }
}
