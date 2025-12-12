package com.runfit.domain.session.repository;

import com.runfit.domain.session.controller.dto.request.SessionSearchCondition;
import com.runfit.domain.session.controller.dto.response.SessionListResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface SessionRepositoryCustom {

    Slice<SessionListResponse> searchSessions(SessionSearchCondition condition, Long userId, Pageable pageable);

    Slice<SessionListResponse> findMyHostedSessions(Long hostUserId, Pageable pageable);
}
