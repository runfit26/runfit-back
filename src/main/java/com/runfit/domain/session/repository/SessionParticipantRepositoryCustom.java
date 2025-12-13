package com.runfit.domain.session.repository;

import com.runfit.domain.session.controller.dto.response.SessionListResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface SessionParticipantRepositoryCustom {

    Slice<SessionListResponse> findParticipatingSessionsByUserId(
        Long userId, String status, Pageable pageable);
}
